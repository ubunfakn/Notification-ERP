import React, { useEffect, useState, useRef, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useSearchParams, useNavigate } from "react-router-dom";
import {
  fetchNotifications,
  retryOne,
  deleteOne,
  // updateOne,
  createNewNotification,
} from "../store/notificationSlice";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import { Tag } from "primereact/tag";
import { Dialog } from "primereact/dialog";
import { ConfirmDialog, confirmDialog } from "primereact/confirmdialog";
import { Paginator } from "primereact/paginator";
import { Skeleton } from "primereact/skeleton";
import { toast } from "react-toastify";
import InputField from "../components/InputField";

const TYPE_OPTIONS = [
  // { label: "All Types", value: "" },
  { label: "Email", value: "EMAIL" },
  { label: "SMS", value: "SMS" },
  { label: "Push", value: "PUSH" },
];

const STATUS_OPTIONS = [
  // { label: "All Status", value: null },
  { label: "Sent", value: "SENT" },
  { label: "Failed", value: "FAILED" },
  { label: "Retry", value: "RETRY" },
];

const PAGE_SIZE = 10;

const statusTagMap = {
  SENT: { severity: "success", icon: "pi-check-circle" },
  FAILED: { severity: "danger", icon: "pi-times-circle" },
  RETRY: { severity: "warning", icon: "pi-refresh" },
};

const typeIconMap = {
  EMAIL: "pi-envelope",
  SMS: "pi-mobile",
  PUSH: "pi-bell",
};

const emptyForm = { userId: "", type: "", message: "", scheduleTime: null };

const NotificationsPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { list, totalRecords, loading, actionLoading } = useSelector(
    (s) => s.notifications,
  );

  const [filters, setFilters] = useState({
    status: searchParams.get("status") ?? "",
    type: searchParams.get("type") ?? "",
    search: "",
  });
  const [page, setPage] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null); // null = create
  const [form, setForm] = useState(emptyForm);
  const [errors, setErrors] = useState({});
  const [retryingId, setRetryingId] = useState(null);

  const loadData = useCallback(() => {
    dispatch(
      fetchNotifications({
        page,
        size: PAGE_SIZE,
        status: filters.status || undefined,
        type: filters.type || undefined,
        keyword: filters.search || undefined,
      }),
    );
  }, [dispatch, page, filters.status, filters.type]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // sync URL filters
  useEffect(() => {
    const p = {};
    if (filters.status) p.status = filters.status;
    if (filters.type) p.type = filters.type;
    setSearchParams(p, { replace: true });
  }, [filters.status, filters.type]);

  const setFilter = (key, val) => {
    setFilters((prev) => ({ ...prev, [key]: val }));
    setPage(0);
  };

  const openCreate = () => {
    setEditTarget(null);
    setForm(emptyForm);
    setErrors({});
    setDialogOpen(true);
  };

  const openEdit = (row) => {
    setEditTarget(row);
    setForm({
      userId: row.userId,
      type: row.type,
      message: row.message,
      scheduleTime: row.scheduleTime ? new Date(row.scheduleTime) : null,
    });
    setErrors({});
    setDialogOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.userId) e.userId = "User ID is required";
    if (!form.type) e.type = "Type is required";
    if (!form.message?.trim()) e.message = "Message is required";
    if (!form.scheduleTime) e.scheduleTime = "Schedule time is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    const formatLocalDateTime = (date) => {
      const pad = (n) => String(n).padStart(2, "0");

      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
    };

    const body = {
      userId: Number(form.userId),
      type: form.type,
      message: form.message,
      scheduleTime:
        form.scheduleTime instanceof Date
          ? formatLocalDateTime(form.scheduleTime)
          : form.scheduleTime,
    };
    try {
      // if (editTarget) {
      //   await dispatch(
      //     updateOne({ id: editTarget.id, body }),
      //   ).unwrap();
      //   toast.success("Notification updated successfully");
      // } else {
        await dispatch(createNewNotification(body)).unwrap();
        toast.success("Notification scheduled!");
        loadData();
      // }
      setDialogOpen(false);
    } catch(error) {
      toast.error(error);
    }
  };

  const handleDelete = (row) => {
    confirmDialog({
      message: `Delete notification #${row.id}? This cannot be undone.`,
      header: "Confirm Delete",
      icon: "pi pi-trash",
      acceptClassName: "p-button-danger",
      accept: async () => {
        try {
          await dispatch(deleteOne(row.id)).unwrap();
          toast.success("Notification deleted");
        } catch(error) {
          toast.error(error);
        }
      },
    });
  };

  const handleRetry = async (row) => {
    setRetryingId(row.id);
    try {
      await dispatch(retryOne(row.id)).unwrap();
      toast.success(`Notification #${row.id} queued for retry`);
    } catch(error) {
      toast.error(error);
    } finally {
      setRetryingId(null);
    }
  };

  const filteredList = filters.search
    ? list.filter(
        (n) =>
          n.message?.toLowerCase().includes(filters.search.toLowerCase()) ||
          String(n.userId).includes(filters.search),
      )
    : list;

  // ── Column Templates ──────────────────────────────────────────────────────
  const idTemplate = (row) => (
    <span className="cell-id">#{row.id}</span>
  );

  const typeTemplate = (row) => (
    <div className="cell-type">
      <i className={`pi ${typeIconMap[row.type] ?? "pi-bell"}`} />
      <span>{row.type}</span>
    </div>
  );

  const statusTemplate = (row) => {
    const cfg = statusTagMap[row.status] ?? {
      severity: "info",
      icon: "pi-question",
    };
    return (
      <Tag
        value={row.status}
        severity={cfg.severity}
        icon={`pi ${cfg.icon}`}
        className="status-tag"
      />
    );
  };

  const scheduleTemplate = (row) =>
    row.scheduleTime
      ? new Date(row.scheduleTime).toLocaleString("en-IN", {
          dateStyle: "medium",
          timeStyle: "short",
        })
      : "—";

  const messageTemplate = (row) => (
    <span className="cell-message" title={row.message}>
      {row.message?.length > 55 ? row.message.slice(0, 52) + "…" : row.message}
    </span>
  );

  const actionsTemplate = (row) => (
    <div className="cell-actions">
      
      {/* <Button
        icon="pi pi-pencil"
        className="p-button-rounded p-button-text edit-btn"
        tooltip="Edit"
        tooltipOptions={{ position: "top" }}
        onClick={() => openEdit(row)}
      /> */}
      <Button
        icon="pi pi-trash"
        className="p-button-rounded p-button-text delete-btn"
        // tooltip="Delete"
        tooltipOptions={{ position: "top" }}
        onClick={() => handleDelete(row)}
      />
      {row.status === "FAILED" && (
        <Button
          icon="pi pi-refresh"
          className="p-button-rounded p-button-text retry-btn"
          // tooltip="Retry"
          tooltipOptions={{ position: "top" }}
          loading={retryingId === row.id}
          onClick={() => handleRetry(row)}
        />
      )}
    </div>
  );

  const FORM_FIELDS = [
    {
      key: "userId",
      label: "User ID",
      type: "text",
      icon: "pi-user",
      placeholder: "e.g. 101",
      required: true,
    },
    {
      key: "type",
      label: "Notification Type",
      type: "dropdown",
      options: TYPE_OPTIONS.filter((o) => o.value),
      required: true,
    },
    {
      key: "scheduleTime",
      label: "Schedule Time",
      type: "calendar",
      required: true,
    },
    {
      key: "message",
      label: "Message",
      type: "textarea",
      placeholder: "Enter notification message…",
      required: true,
    },
  ];

  return (
    <div className="page notifications-page">
      <ConfirmDialog />

      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">
            {totalRecords} record{totalRecords !== 1 ? "s" : ""}
            {filters.status || filters.type ? " (filtered)" : ""}
          </p>
        </div>
        <div className="page-header-actions">
          <Button
            label="Dashboard"
            icon="pi pi-th-large"
            className="p-button-text cyan-btn-text"
            onClick={() => navigate("/dashboard")}
          />
          <Button
            label="New Notification"
            icon="pi pi-plus"
            className="p-button cyan-btn"
            onClick={openCreate}
          />
        </div>
      </div>

      {/* Filters */}
      <div className="filters-bar">
        <InputField
          type="text"
          placeholder="Search by message or user ID…"
          value={filters.search}
          onChange={(v) => setFilter("search", v)}
          icon="pi-search"
          className="filter-search"
          style={{ padding: "10px" }}
        />
        <InputField
          type="dropdown"
          placeholder="Filter by type"
          value={filters.type}
          options={TYPE_OPTIONS}
          onChange={(v) => setFilter("type", v)}
          className="filter-select"
        />
        <InputField
          type="dropdown"
          placeholder="Filter by status"
          value={filters.status}
          options={STATUS_OPTIONS}
          onChange={(v) => setFilter("status", v)}
          className="filter-select"
        />
        {(filters.status || filters.type || filters.search) && (
          <Button
            icon="pi pi-filter-slash"
            label="Clear"
            className="p-button-outlined p-button-sm clear-btn"
            onClick={() => {
              setFilters({ status: "", type: "", search: "" });
              setPage(0);
            }}
          />
        )}
      </div>

      {/* Table */}
      <div className="table-card">
        {loading ? (
          <div className="skeleton-table">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} height="44px" className="skeleton-row" />
            ))}
          </div>
        ) : (
          <DataTable
            value={filteredList}
            emptyMessage={
              <div className="empty-state">
                <i className="pi pi-inbox" />
                <span>No notifications found</span>
              </div>
            }
            className="notif-table"
            rowClassName={(row) => `row-status-${row?.status?.toLowerCase()}`}
          >
            <Column header="ID" body={idTemplate} style={{ width: "80px" }} />
            <Column field="userId" header="User ID" style={{ width: "90px" }} />
            <Column
              header="Type"
              body={typeTemplate}
              style={{ width: "110px" }}
            />
            <Column header="Message" body={messageTemplate} />
            <Column
              header="Scheduled"
              body={scheduleTemplate}
              style={{ width: "180px" }}
            />
            <Column
              header="Status"
              body={statusTemplate}
              style={{ width: "110px" }}
            />
            <Column
              header="Actions"
              body={actionsTemplate}
              style={{ width: "140px" }}
            />
          </DataTable>
        )}
      </div>

      {/* Pagination */}
      {!loading && totalRecords > PAGE_SIZE && (
        <Paginator
          first={page * PAGE_SIZE}
          rows={PAGE_SIZE}
          totalRecords={totalRecords}
          onPageChange={(e) => setPage(e.page)}
          className="table-paginator"
        />
      )}

      {/* Create / Edit Dialog */}
      <Dialog
        visible={dialogOpen}
        onHide={() => setDialogOpen(false)}
        header={
          <span className="dialog-header">
            <i
              className={`pi ${editTarget ? "pi-pencil" : "pi-plus-circle"}`}
            />
            {editTarget ? "Edit Notification" : "New Notification"}
          </span>
        }
        className="notif-dialog"
        style={{ width: "480px", padding: "18px" }}
        modal
        draggable={false}
      >
        <div className="dialog-body" style={{ marginTop: "14px" }}>
          {FORM_FIELDS.map(({ key, ...fieldProps }) => (
            <InputField
              key={key}
              value={form[key]}
              onChange={(v) => setForm((prev) => ({ ...prev, [key]: v }))}
              error={errors[key]}
              {...fieldProps}
            />
          ))}
        </div>
        <div className="dialog-footer">
          <Button
            label="Cancel"
            icon="pi pi-times"
            className="p-button-text"
            onClick={() => setDialogOpen(false)}
          />
          <Button
            label={editTarget ? "Update" : "Schedule"}
            icon={editTarget ? "pi pi-check" : "pi pi-send"}
            className="cyan-btn"
            onClick={handleSave}
            loading={actionLoading}
          />
        </div>
      </Dialog>
    </div>
  );
};

export default NotificationsPage;
