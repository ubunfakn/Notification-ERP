import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import {
  getAllNotifications,
  createNotification,
  retryNotification,
  deleteNotification,
  // updateNotification,
} from "../api/notificationApi";

export const fetchNotifications = createAsyncThunk(
  "notifications/fetchAll",
  async (params, { rejectWithValue }) => {
    try { 
      const data = await getAllNotifications(params);
      return data?.data
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

export const createNewNotification = createAsyncThunk(
  "notifications/create",
  async (body, { rejectWithValue }) => {
    try {
      return await createNotification(body);
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

export const retryOne = createAsyncThunk(
  "notifications/retry",
  async (id, { rejectWithValue }) => {
    try {
      return await retryNotification(id);
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

export const deleteOne = createAsyncThunk(
  "notifications/delete",
  async (id, { rejectWithValue }) => {
    try {
      await deleteNotification(id);
      return id;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  }
);

// export const updateOne = createAsyncThunk(
//   "notifications/update",
//   async ({ id, body }, { rejectWithValue }) => {
//     try {
//       return await updateNotification(id, body);
//     } catch (err) {
//       return rejectWithValue(err.message);
//     }
//   }
// );

const notificationsSlice = createSlice({
  name: "notifications",
  initialState: {
    list: [],
    totalRecords: 0,
    loading: false,
    error: null,
    actionLoading: false,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        console.log(action.payload)
        state.loading = false;
        state.list = action.payload?.notifications ?? action.payload ?? [];
        state.totalRecords = action.payload?.totalElements ?? state.list.length;
      })
      .addCase(fetchNotifications.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(retryOne.pending, (state) => { state.actionLoading = true; })
      .addCase(retryOne.fulfilled, (state, action) => {
        state.actionLoading = false;
        const idx = state.list.findIndex(n => n.id === action.payload?.id);
        if (idx !== -1) state.list[idx] = action.payload;
      })
      .addCase(retryOne.rejected, (state) => { state.actionLoading = false; })
      .addCase(deleteOne.fulfilled, (state, action) => {
        state.list = state.list.filter(n => n.id !== action.payload);
        state.totalRecords = Math.max(0, state.totalRecords - 1);
      })
      // .addCase(updateOne.fulfilled, (state, action) => {
      //   const idx = state.list.findIndex(n => n.id === action.payload?.id);
      //   if (idx !== -1) state.list[idx] = action.payload;
      // });
  },
});

export default notificationsSlice.reducer;