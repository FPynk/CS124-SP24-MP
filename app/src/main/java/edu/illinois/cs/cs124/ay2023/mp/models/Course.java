package edu.illinois.cs.cs124.ay2023.mp.models;
import org.jetbrains.annotations.NotNull;

public class Course extends Summary {
  private String description;

  /**
   * Jackson serialization requires a no-argument constructor.
   */
  public Course() {
    super(); // Calls the no-arg constructor of the Summary class
  }

  /**
   * Create a Course with the provided fields.
   *
   * @param setSubject    the department for this Course
   * @param setNumber     the number for this Course
   * @param setLabel      the label for this Course
   * @param setDescription the description for this Course
   */
  public Course(@NotNull String setSubject,
                @NotNull String setNumber,
                @NotNull String setLabel,
                @NotNull String setDescription) {
    super(setSubject, setNumber, setLabel); // Calls the constructor of the Summary class
    this.description = setDescription;
  }

  /**
   * Get the description for this Course.
   *
   * @return the description for this Course
   */
  @NotNull
  public String getDescription() {
    return description;
  }

  /**
   * Set the description for this Course.
   *
   * @param setDescription the description to set
   */
  public void setDescription(@NotNull String setDescription) {
    this.description = setDescription;
  }
}
