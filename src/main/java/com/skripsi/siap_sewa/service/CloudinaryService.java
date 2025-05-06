package com.skripsi.siap_sewa.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validasi ukuran file (max 1MB)
        if (file.getSize() > 1048576) {
            throw new IllegalArgumentException("Ukuran gambar maksimal 1MB");
        }

        // Validasi tipe file
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg"))) {
            throw new IllegalArgumentException("Format gambar harus JPEG, JPG, atau PNG");
        }

        Map<?, ?> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "customer_profiles",
                                "resource_type", "image"
                        ));
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Gagal menghapus gambar dari Cloudinary: {}", e.getMessage());
            throw new IOException("Gagal menghapus gambar dari Cloudinary", e);
        }
    }
}
