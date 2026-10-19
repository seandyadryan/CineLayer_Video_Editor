# Impor terjemahan listing Play Store

Paket: `com.deploydulupulangnanti.cinelayer`, versi **1.0.0**, version code **1**.

Gunakan `CineLayer-Play-Store-Translations.csv` pada menu **Impor terjemahan dengan AI**. CSV UTF-8 ini berisi nama aplikasi, deskripsi singkat, dan deskripsi lengkap untuk **English (United States) / en-US** serta **Indonesia / id**. Pilih bahasa tambahan yang ingin dibuat di Play Console dengan teks Inggris sebagai sumber. Nama merek CineLayer, alamat email, serta nama developer dipertahankan.

1. Buka listing Play Store aplikasi, lalu menu pengelolaan bahasa/terjemahan.
2. Pilih **Impor terjemahan dengan AI** dan unggah CSV tersebut.
3. Cocokkan kolom `locale`, `title`, `short_description`, dan `full_description` dengan bahasa, nama aplikasi, deskripsi singkat, dan deskripsi lengkap jika diminta.
4. Periksa bahasa dan pemetaan kolom pada pratinjau, lalu terapkan ke listing yang diinginkan.

Google mendokumentasikan impor AI dari file terstruktur seperti CSV, tetapi tidak memublikasikan satu skema header tetap pada sumber yang diperiksa. File ini disusun agar isi dan bahasanya dapat dikenali AI; penerimaannya belum diuji di akun Play Console Anda. Bila konsol menampilkan template ekspor tertentu, format template akun tersebut menjadi acuan.

Terjemahan ini hanya untuk **listing toko**. Antarmuka aplikasi saat ini tetap berbahasa Inggris; kedua deskripsi menjelaskan hal tersebut. Terjemahan Indonesia adalah draf AI yang dapat Anda sunting sebelum diterapkan. Teks Inggris memakai deskripsi proyek yang sudah ada, ditambah keterangan bahasa antarmuka. Grafik dan screenshot tetap memakai aset Inggris yang sudah disiapkan.

## Nama dan catatan rilis

File ada di `store/releases/1.0.0/`:

- `release-name.txt`: isi kolom nama rilis dengan `1.0.0 (1) - Initial release`.
- `play-console-en-US.txt`: catatan siap ditempel dengan tag `<en-US>`.
- `play-console-id.txt`: catatan siap ditempel dengan tag `<id>`.
- `play-console-en-US-id.txt`: gabungan dua bahasa; pakai setelah kedua bahasa tersedia di listing.
- `release-notes-en-US.txt` dan `release-notes-id.txt`: isi tanpa tag untuk kolom satu bahasa.

Catatan rilis menggambarkan rilis perdana dan fitur yang sudah tersedia. Tidak ada klaim perbaikan bug, fitur editor multitrack, atau penambahan bahasa antarmuka. Perubahan metadata ini tidak mengubah versi APK/AAB.

## Sumber resmi

Diperiksa 19 Oktober 2026:

- [Google Play Translation services](https://google.play/business/translationservices/) menjelaskan impor listing dari CSV atau Google Sheet melalui Gemini.
- [Translate and localize your app](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en) memuat kode bahasa dan membedakan terjemahan listing dari app strings.
- [Menyiapkan dan meluncurkan rilis](https://support.google.com/googleplay/android-developer/answer/9859348?hl=id) menjelaskan nama rilis internal, tag bahasa pada baris terpisah, dan batas 500 karakter Unicode per bahasa.
