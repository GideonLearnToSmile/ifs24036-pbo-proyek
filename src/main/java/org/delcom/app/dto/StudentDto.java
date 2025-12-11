package org.delcom.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public class StudentDto {
    private UUID id;

    @NotBlank(message = "NIM tidak boleh kosong")
    private String nim;

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    @NotBlank(message = "Jurusan tidak boleh kosong")
    private String major;

    @NotNull(message = "Tahun Masuk tidak boleh kosong")
    private Integer entryYear;

    private MultipartFile photoFile; // Untuk upload
    private String existingPhotoPath; // Untuk display saat edit

    // Getters Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getEntryYear() { return entryYear; }
    public void setEntryYear(Integer entryYear) { this.entryYear = entryYear; }
    public MultipartFile getPhotoFile() { return photoFile; }
    public void setPhotoFile(MultipartFile photoFile) { this.photoFile = photoFile; }
    public String getExistingPhotoPath() { return existingPhotoPath; }
    public void setExistingPhotoPath(String existingPhotoPath) { this.existingPhotoPath = existingPhotoPath; }
}