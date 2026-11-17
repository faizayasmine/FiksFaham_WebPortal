# Panduan Deploy FiksFaham Web Portal ke GitHub Pages

Portal Web sudah dikonfigurasi dengan `HashRouter`, `VITE_BASE_PATH` dinamis, dan workflow otomatis di `.github/workflows/deploy-web-portal.yml`.

Anda cukup mengikuti **4 langkah berikut** setelah mendorong (push) kode ke GitHub:

---

## 1. Push Repository ke GitHub
Pastikan branch utama di GitHub bernama **`main`** (atau `master`), lalu push seluruh proyek ini termasuk folder `.github/` dan `web-portal/`.

> File `web-portal/.env.local` otomatis dikecualikan oleh `.gitignore` sehingga rahasia lokal tidak ikut masuk ke riwayat Git.

---

## 2. Aktifkan GitHub Pages via GitHub Actions
1. Buka repository Anda di GitHub → tab **Settings**.
2. Di menu kiri, klik **Pages**.
3. Pada bagian **Build and deployment → Source**, pilih **`GitHub Actions`** (bukan *Deploy from a branch*).

---

## 3. Isi 6 Secret Firebase di GitHub Repository
Agar hasil build di GitHub Actions terhubung ke Firebase `fiksfaham`:
1. Buka **Settings → Secrets and variables → Actions → Repository secrets**.
2. Klik **New repository secret** untuk setiap variabel berikut (salin nilainya dari file `web-portal/.env.local` di laptop Anda):
   - `VITE_FIREBASE_API_KEY`
   - `VITE_FIREBASE_AUTH_DOMAIN`
   - `VITE_FIREBASE_PROJECT_ID`
   - `VITE_FIREBASE_STORAGE_BUCKET`
   - `VITE_FIREBASE_MESSAGING_SENDER_ID`
   - `VITE_FIREBASE_APP_ID`
3. Setelah semua terisi, buka tab **Actions → Deploy FiksFaham Web Portal to GitHub Pages → Re-run jobs** (atau **Run workflow**).

---

## 4. Daftarkan Domain GitHub Pages di Firebase Auth (Wajib untuk Login Google!)
Setelah workflow selesai hijau dan URL GitHub Pages tampil (misal `https://username.github.io/nama-repo/`):
1. Buka **Firebase Console** → pilih project **`fiksfaham`**.
2. Masuk ke **Authentication → Settings → Authorized domains**.
3. Klik **Add domain** lalu masukkan host GitHub Pages Anda **tanpa `https://` dan tanpa nama repo**, contoh:
   ```text
   username.github.io
   ```
4. Jika belum men-deploy rules Firestore terbaru (`staff_invites` & `certificates_public`), jalankan sekali dari terminal lokal:
   ```powershell
   firebase deploy --only firestore:rules
   ```

Selesai! Buka URL GitHub Pages Anda — gambar 3D login, routing `/#/login`, `/#/verify`, undangan staf via email, dan penilaian capstone siap digunakan.
