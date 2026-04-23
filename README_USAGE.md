# README (Alur Penggunaan Aplikasi)

Dokumen ini menjelaskan cara memakai aplikasi dari sisi user dan admin.

## 1) Login / Register

- Buka halaman login.
- Jika belum punya akun, pilih Register lalu buat akun.
- Setelah login berhasil, aplikasi menyimpan token (Bearer) untuk request API.

## 2) Memilih Menu (Makanan / Minuman / Extra)

- Pilih tab kategori: Makanan, Minuman, atau Extra.
- Tekan tombol `+` untuk menambah jumlah item, `-` untuk mengurangi.
- Total item dan total harga akan tampil di bar “Keranjang”.

## 3) Checkout (Membuat Pesanan)

- Tekan tombol “Checkout”.
- Sheet “Pesanan” akan muncul berisi ringkasan item.
- Tekan “Buat Pesanan” untuk membuat order.

Status setelah order dibuat:
- Status pesanan: `Belum dibayar`
- Aplikasi akan mengarahkan ke sheet “Pembayaran”.

## 4) Pembayaran

Di sheet “Pembayaran”, pilih metode:

### a) Cash
- Pilih metode “Cash”.
- Tekan “Bayar”.
- Status menjadi `Lunas`.

### b) QRIS
- Pilih metode “QRIS”.
- Tekan “Bayar”.
- Status menjadi `Lunas`.

### c) Bank
- Pilih metode “Bank”.
- Pilih “Rekening tujuan” (BRI / Mandiri / BCA).
- Tekan “Bayar”.
- Jika saldo cukup, status menjadi `Lunas` dan rekening tujuan akan tercetak di struk.

## 5) Struk (Receipt)

- Setelah pembayaran, sheet “Struk” menampilkan:
  - Total
  - Status (Lunas / Belum dibayar)
  - Metode pembayaran
  - Rekening (khusus BANK)
  - Dibayar & Kembalian
- Tekan “Selesai” untuk menutup sheet dan kembali ke aplikasi.

## 6) Riwayat Pesanan

- Tekan ikon “Riwayat” pada header.
- Akan tampil list pesanan milik akun yang sedang login.
- Tombol pada setiap card:
  - “Lihat Struk” jika sudah lunas
  - “Bayar” jika belum dibayar

## 7) Batalkan Pesanan (Agar Riwayat Tidak Penuh)

Jika pesanan masih `Belum dibayar`, kamu bisa membatalkan agar tidak memenuhi history:

- Dari sheet “Pembayaran”: tekan tombol “Batalkan Pesanan”
- Dari “Riwayat”: tekan tombol “Batalkan” pada pesanan yang belum dibayar

Efek pembatalan:
- Pesanan akan dihapus dari sistem (tidak muncul lagi di riwayat).

## 8) Admin (Kelola Menu)

Syarat: akun memiliki role `ROLE_ADMIN`.

- Setelah login sebagai admin, akan muncul tombol “Admin Menu” di header.
- Admin bisa:
  - Tambah menu
  - Edit menu
  - Hapus menu

Catatan:
- Fitur konfirmasi pembayaran admin sudah dihapus. Semua pembayaran diproses langsung.

