import React from "react";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { Calendar } from "primereact/calendar";
import { InputTextarea } from "primereact/inputtextarea";

/**
 * Reusable field component.
 * type: "text" | "dropdown" | "calendar" | "textarea"
 */
const InputField = ({
  type = "text",
  label,
  value,
  onChange,
  options,
  placeholder,
  icon,
  className = "",
  required = false,
  error,
  disabled = false,
  ...rest
}) => {
  const renderInput = () => {
    switch (type) {
      case "dropdown":
        return (
          <Dropdown
            value={value}
            options={options}
            onChange={(e) => onChange(e.value)}
            placeholder={placeholder ?? `Select ${label}`}
            className={`w-full custom-dropdown ${error ? "p-invalid" : ""}`}
            disabled={disabled}
            {...rest}
          />
        );
      case "calendar":
        return (
          <Calendar
            value={value}
            onChange={(e) => onChange(e.value)}
            showTime
            showIcon
            placeholder={placeholder ?? `Pick ${label}`}
            className={`w-full ${error ? "p-invalid" : ""}`}
            disabled={disabled}
            {...rest}
          />
        );
      case "textarea":
        return (
          <InputTextarea
            value={value ?? ""}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder}
            rows={3}
            className={`w-full ${error ? "p-invalid" : ""}`}
            disabled={disabled}
            {...rest}
          />
        );
      default:
        return (
          <span className={`p-input-icon-left w-full ${icon ? "" : "no-icon"}`}>
            {icon && <i className={`pi ${icon}`} />}
            <InputText
              value={value ?? ""}
              onChange={(e) => onChange(e.target.value)}
              placeholder={placeholder}
              className={`w-full ${error ? "p-invalid" : ""}`}
              disabled={disabled}
              {...rest}
            />
          </span>
        );
    }
  };

  return (
    <div className={`field-wrapper ${className}`}>
      {label && (
        <label className="field-label">
          {label}
          {required && <span className="field-required">*</span>}
        </label>
      )}
      {renderInput()}
      {error && <small className="field-error">{error}</small>}
    </div>
  );
};

export default InputField;