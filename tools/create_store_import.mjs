import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Workbook } from '@oai/artifact-tool';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const output = path.join(root, 'store', 'translations');
const releases = path.join(root, 'store', 'releases', '1.0.0');
const previewDir = path.join(root, '.local', 'store-import');
const englishShort = (await fs.readFile(path.join(root, 'store/listing/short-description.txt'), 'utf8')).trim();
const englishFull = (await fs.readFile(path.join(root, 'store/listing/full-description.txt'), 'utf8')).trim()
  + '\n\nApp interface: English.';
const indonesianFull = `CineLayer Video Editor membantu Anda menemukan klip yang tepat dan menyiapkan potongan video sederhana langsung di perangkat Android.

Kumpulkan video, audio, dan gambar yang Anda pilih dalam satu pustaka media. Cari berdasarkan nama file, catatan, tag warna, frame rate, dan rasio aspek. Batasi tampilan menurut sumber atau jenis media, tandai favorit, dan urutkan berdasarkan tanggal.

TEMUKAN MEDIA ANDA
- Impor file pilihan melalui pemilih file bawaan Android.
- Cari nama file dan catatan, nama tag warna secara utuh, frame rate, serta rasio aspek yang tersedia.
- Pilih semua sumber, file impor, atau sampel bawaan.
- Filter video, audio, dan gambar.
- Tambahkan catatan dan tag warna melalui menu pratinjau.
- Tandai media favorit agar mudah ditemukan kembali.
- Urutkan pustaka berdasarkan tanggal.

PRATINJAU SEBELUM MEMOTONG
Buka gambar, dengarkan audio, atau tonton video sebelum menentukan langkah berikutnya. Dukungan media bergantung pada format dan codec yang tersedia di perangkat.

Coba klip, audio, dan gambar pantai buatan AI yang disertakan. Aset bawaan diberi label Sample agar mudah dibedakan dari file impor Anda.

POTONG DAN SIMPAN
Pilih waktu awal dan akhir video, lalu simpan hasilnya sebagai MP4 ke lokasi pilihan Anda. Pemotongan mengikuti keyframe, sehingga hasilnya dapat dimulai sedikit sebelum titik awal yang dipilih. Trek video dan audio yang didukung disalin tanpa pengodean ulang. Kompatibilitas ekspor bergantung pada file sumber dan perangkat.

BEKERJA SECARA LOKAL
Pencarian, pratinjau, dan pemotongan berjalan di perangkat. Aplikasi tidak mengunggah media Anda dan tidak menggunakan akun, iklan, atau analitik. Pemilih file Android dapat menampilkan penyedia penyimpanan cloud. Transfer melalui penyedia yang Anda pilih mengikuti ketentuan penyedia tersebut.

PUSTAKA ANDA, PILIHAN ANDA
CineLayer menyimpan entri pustaka, catatan, tag warna, favorit, dan informasi pemotongan secara lokal. Menghapus entri dari pustaka tidak menghapus file media asli. Video hasil ekspor tetap tersimpan di lokasi yang Anda pilih.

Rilis ini berfokus pada pencarian, pengelolaan, pratinjau media, dan pemotongan satu video. Belum tersedia timeline multitrack, transisi, efek, atau komposisi video.

Memerlukan Android 9 atau lebih baru.
Bahasa antarmuka aplikasi: Inggris.

Dukungan: seandyadryan2@gmail.com
Developer: PT Deploy Dulu Pulang Nanti`;
const rows = [
  ['locale', 'title', 'short_description', 'full_description'],
  ['en-US', 'CineLayer Video Editor', englishShort, englishFull],
  ['id', 'CineLayer Video Editor', 'Temukan klip, kelola media, dan potong video langsung di perangkat Anda.', indonesianFull],
];
const releaseName = '1.0.0 (1) - Initial release';
const notes = {
  'en-US': `Initial release of CineLayer.
- Import videos, audio and images.
- Search filenames, notes, color tags and available metadata.
- Filter media and save favorites.
- Preview media and trim individual videos to MP4.
- Work locally without accounts, ads or analytics.`,
  id: `Rilis perdana CineLayer.
- Impor video, audio, dan gambar.
- Cari nama file, catatan, tag warna, dan metadata yang tersedia.
- Filter media dan simpan favorit.
- Pratinjau media dan potong satu video menjadi MP4.
- Bekerja secara lokal tanpa akun, iklan, atau analitik.`,
};

const length = value => Array.from(value).length;
const limits = rows.slice(1).map(([locale, title, short, full]) => {
  if (length(title) > 30 || length(short) > 80 || length(full) > 4000)
    throw new Error(`Listing limit exceeded: ${locale}`);
  if (length(notes[locale]) > 500) throw new Error(`Release notes exceed 500: ${locale}`);
  return {locale, title: length(title), shortDescription: length(short), fullDescription: length(full), releaseNotes: length(notes[locale])};
});

await fs.mkdir(output, {recursive:true});
await fs.mkdir(releases, {recursive:true});
await fs.mkdir(previewDir, {recursive:true});
const book = Workbook.create();
const sheet = book.worksheets.add('Listing translations');
sheet.showGridLines = false;
sheet.getRange('A1:D3').values = rows;
sheet.getRange('A1:D3').setNumberFormat('@');
sheet.getRange('A1:D3').format.font = {name:'Arial',size:11,color:'#17212E'};
sheet.getRange('A1:D3').format.wrapText = true;
sheet.getRange('A1:D3').format.verticalAlignment = 'top';
sheet.getRange('A1:D1').format.fill = '#24394C';
sheet.getRange('A1:D1').format.font = {name:'Arial',size:11,bold:true,color:'#FFFFFF'};
sheet.getRange('A1:D1').format.rowHeightPx = 36;
sheet.getRange('A1:A3').format.columnWidthPx = 85;
sheet.getRange('B1:B3').format.columnWidthPx = 185;
sheet.getRange('C1:C3').format.columnWidthPx = 245;
sheet.getRange('D1:D3').format.columnWidthPx = 710;
sheet.getRange('A2:D3').format.rowHeightPx = 1030;
book.recalculate();
console.log((await book.inspect({kind:'table',range:"'Listing translations'!A1:D3",include:'values,formulas',tableMaxRows:3,tableMaxCols:4,tableMaxCellChars:60,maxChars:1600})).ndjson);
console.log((await book.inspect({kind:'match',searchTerm:'#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!',options:{useRegex:true,maxResults:20},maxChars:600})).ndjson);

// Serialize authored cells as RFC 4180 CSV; retain embedded description line breaks.
const csv = sheet.getRange('A1:D3').values.map(row => row.map(value => '"'+String(value??'').replaceAll('"','""')+'"').join(',')).join('\r\n')+'\r\n';
const target = path.join(output,'CineLayer-Play-Store-Translations.csv');
await fs.writeFile(target,csv,'utf8');
const roundtrip = await Workbook.fromCSV(await fs.readFile(target,'utf8'),{sheetName:'Import check'});
const restored = roundtrip.worksheets.getItemAt(0).getRange('A1:D3').values;
if (JSON.stringify(restored)!==JSON.stringify(rows)) throw new Error('CSV round-trip mismatch');
const rendered = await book.render({sheetName:'Listing translations',range:'A1:D3',scale:1,format:'png'});
await fs.writeFile(path.join(previewDir,'review.png'),new Uint8Array(await rendered.arrayBuffer()));

await fs.writeFile(path.join(releases,'release-name.txt'),releaseName+'\n','utf8');
for (const [locale,text] of Object.entries(notes)) {
  await fs.writeFile(path.join(releases,`release-notes-${locale}.txt`),text+'\n','utf8');
  await fs.writeFile(path.join(releases,`play-console-${locale}.txt`),`<${locale}>\n${text}\n</${locale}>\n`,'utf8');
}
await fs.writeFile(path.join(releases,'play-console-en-US-id.txt'),Object.entries(notes).map(([locale,text])=>`<${locale}>\n${text}\n</${locale}>`).join('\n\n')+'\n','utf8');
await fs.writeFile(path.join(output,'validation.json'),JSON.stringify({packageName:'com.deploydulupulangnanti.cinelayer',versionName:'1.0.0',versionCode:1,releaseName,locales:limits,csvRoundtrip:true,playConsoleImportTested:false},null,2)+'\n','utf8');
console.log(JSON.stringify({releaseName,locales:limits,csvRoundtrip:true}));
