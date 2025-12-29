package org.forrisco.core.imports;

import java.util.Objects;

public class ImportError {
  private int row;
  private String field;
  private String message;

  public ImportError() {
  }

  public ImportError(int row, String field, String message) {
    this.row = row;
    this.field = field;
    this.message = message;
  }

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ImportError))
      return false;
    ImportError that = (ImportError) o;
    return row == that.row &&
        Objects.equals(field, that.field) &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(row, field, message);
  }

  @Override
  public String toString() {
    return "ImportError{" +
        "row=" + row +
        ", field='" + field + '\'' +
        ", message='" + message + '\'' +
        '}';
  }
}
