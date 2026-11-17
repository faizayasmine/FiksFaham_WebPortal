package com.example.data.model

/**
 * Model data Inspirasi Desain (Tahap 11).
 * Disimpan di Firestore koleksi "inspirasi".
 *
 * Atribut:
 * - id: ID Dokumen Firestore
 * - judul: Judul karya / inspirasi desain
 * - gambarUrl: URL gambar pratinjau / thumbnail
 * - deskripsi: Deskripsi singkat tentang karya / ide
 * - link: Tautan eksternal (Dribbble, Behance, Figma, situs web, dll.)
 * - tanggal: Label tanggal publikasi (misal: "1 Nov 2026")
 * - createdAt: Timestamp millis pembuatan
 */
data class InspirasiItem(
    val id: String = "",
    val judul: String = "",
    val gambarUrl: String = "",
    val deskripsi: String = "",
    val link: String = "",
    val tanggal: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
