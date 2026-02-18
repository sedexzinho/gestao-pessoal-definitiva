import { forwardRef } from "react";

export default forwardRef(function Input(
  {
    label,
    type = "text",
    name,
    error,
    placeholder,
    required = false,
    disabled = false,
    className = "",
    ...props
  },
  ref,
) {
  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <input
        ref={ref}
        type={type}
        id={name}
        name={name}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 
          ${
            error
              ? "border-red-500 focus:border-red-500"
              : "border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white"
          } 
          ${disabled ? "bg-gray-100 dark:bg-gray-700 cursor-not-allowed" : "bg-white dark:bg-gray-800"}`}
        {...props}
      />
      {error && <p className="mt-1 text-sm text-red-500">{error}</p>}
    </div>
  );
});
