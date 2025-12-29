package org.forrisco.core.imports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ImportResult implements Serializable {
  private int imported;
  private int skipped;
  private List<ImportError> errors;
  private List<String> warnings;

  public ImportResult() {
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
  }

  public ImportResult(int imported, int skipped, List<ImportError> errors, List<String> warnings) {
    this.imported = imported;
    this.skipped = skipped;
    this.errors = (errors != null) ? new ArrayList<>(errors) : new ArrayList<>();
    this.warnings = (warnings != null) ? new ArrayList<>(warnings) : new ArrayList<>();
  }

  public static ImportResult empty() {
    return new ImportResult(0, 0, Collections.emptyList(), Collections.emptyList());
  }

  public int getImported() {
    return imported;
  }

  public void setImported(int imported) {
    this.imported = imported;
  }

  public int getSkipped() {
    return skipped;
  }

  public void setSkipped(int skipped) {
    this.skipped = skipped;
  }

  public List<ImportError> getErrors() {
    return errors;
  }

  public void setErrors(List<ImportError> errors) {
    this.errors = (errors != null) ? new ArrayList<>(errors) : new ArrayList<>();
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = (warnings != null) ? new ArrayList<>(warnings) : new ArrayList<>();
  }

  public void addError(ImportError error) {
    if (this.errors == null)
      this.errors = new ArrayList<>();
    this.errors.add(error);
  }

  public void addWarning(String warning) {
    if (this.warnings == null)
      this.warnings = new ArrayList<>();
    this.warnings.add(warning);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ImportResult))
      return false;
    ImportResult that = (ImportResult) o;
    return imported == that.imported &&
        skipped == that.skipped &&
        Objects.equals(errors, that.errors) &&
        Objects.equals(warnings, that.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(imported, skipped, errors, warnings);
  }

  @Override
  public String toString() {
    return "ImportResult{" +
        "imported=" + imported +
        ", skipped=" + skipped +
        ", errors=" + errors +
        ", warnings=" + warnings +
        '}';
  }
}
