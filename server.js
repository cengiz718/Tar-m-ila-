const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');

const HOST = '0.0.0.0';
const PORT = process.env.NGINX_PORT
  ? Number(process.env.DEFAULT_APP_PORT || 3000)
  : Number(process.env.PORT || process.env.DEFAULT_APP_PORT || 3000);

const ROOT_DIR = __dirname;
const APK_CANDIDATES = [
  path.join(ROOT_DIR, '.build-outputs', 'app-debug.apk'),
  path.join(ROOT_DIR, 'public', 'TarimMeteorolojiAI-v2.0.apk'),
  path.join(ROOT_DIR, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
];

function findExistingApk() {
  for (const candidate of APK_CANDIDATES) {
    try {
      const stat = fs.statSync(candidate);
      if (stat.isFile() && stat.size > 0) {
        return { filePath: candidate, sizeBytes: stat.size };
      }
    } catch (_) {
      // Ignore missing candidates
    }
  }
  return null;
}

function formatBytes(bytes) {
  if (!bytes) return '0 MB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function renderHtml(apkInfo) {
  const apkBadge = apkInfo
    ? `<span class="badge ok">APK Hazır (${formatBytes(apkInfo.sizeBytes)})</span>`
    : `<span class="badge warn">Android SDK Derleme Gerektirir</span>`;

  const apkAction = apkInfo
    ? `<a class="btn primary" href="/download/app-debug.apk" download="TarimMeteorolojiAI-v2.0.apk">⬇️ Android APK İndir (${formatBytes(apkInfo.sizeBytes)})</a>`
    : `<span class="hint">Bu Cloud Run web örneğinde APK ikili dosyası bulunmuyor; GitHub Actions iş akışı (.github/workflows/build-apk.yml) veya Android Gradle ile APK üretebilirsiniz.</span>`;

  return `<!doctype html>
<html lang="tr">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Tarım Meteoroloji AI — Tarımsal Karar Desteği & Android APK</title>
  <meta name="description" content="Çiftçiler ve ziraat uzmanları için konum bazlı 7 günlük tarımsal hava durumu, anlık ilaçlama uygunluk analizi, Room çevrimdışı önbellek, sesli arama ve foto/video bitki hastalığı teşhisi." />
  <style>
    :root {
      --primary: #1B5E20;
      --primary-dark: #134417;
      --primary-light: #E8F5E9;
      --border: #C8E6C9;
      --bg: #F4F7F4;
      --surface: #FFFFFF;
      --text: #1F2933;
      --muted: #52606D;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: var(--bg);
      color: var(--text);
      line-height: 1.5;
    }
    header {
      background: var(--primary);
      color: #fff;
      padding: 20px 16px;
    }
    .container {
      max-width: 880px;
      margin: 0 auto;
      padding: 16px;
    }
    .header-inner {
      max-width: 880px;
      margin: 0 auto;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
    }
    h1 { margin: 0; font-size: 1.35rem; }
    .sub { font-size: 0.88rem; opacity: 0.9; margin-top: 4px; }
    .card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 18px;
      margin-bottom: 14px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    }
    .card h2 {
      margin: 0 0 10px 0;
      font-size: 1rem;
      color: var(--primary);
    }
    .badge {
      display: inline-block;
      font-size: 0.78rem;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: 999px;
    }
    .badge.ok { background: #C8E6C9; color: #1B5E20; }
    .badge.warn { background: #FFF8E1; color: #E65100; }
    .btn {
      display: inline-block;
      text-decoration: none;
      padding: 11px 16px;
      border-radius: 10px;
      font-weight: 700;
      font-size: 0.92rem;
      cursor: pointer;
      border: 1px solid transparent;
    }
    .btn.primary { background: var(--primary); color: #fff; }
    .btn.primary:hover { background: var(--primary-dark); }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
      gap: 12px;
    }
    .feat {
      background: #F9FBF9;
      border: 1px solid #E2ECE3;
      border-radius: 12px;
      padding: 12px;
    }
    .feat strong { color: var(--primary); display: block; margin-bottom: 4px; }
    .hint { color: var(--muted); font-size: 0.88rem; display: block; margin-top: 8px; }
    .row { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
    select, button.calc {
      padding: 9px 12px;
      border-radius: 8px;
      border: 1px solid var(--border);
      font-size: 0.92rem;
    }
    button.calc {
      background: var(--primary);
      color: #fff;
      font-weight: 700;
      cursor: pointer;
    }
    #weatherBox {
      margin-top: 12px;
      padding: 12px;
      border-radius: 10px;
      background: var(--primary-light);
      border: 1px solid var(--border);
      font-size: 0.92rem;
    }
  </style>
</head>
<body>
  <header>
    <div class="header-inner">
      <div>
        <h1>🌾 TARIM METEOROLOJİ AI v2.0 PRO</h1>
        <div class="sub">Android Kotlin / Jetpack Compose Tarımsal Karar Desteği • Program Tasarımı: Mehmet Cengiz Akdağ</div>
      </div>
      ${apkBadge}
    </div>
  </header>

  <main class="container">
    <section class="card">
      <h2>📱 Android Uygulama Paketi (APK)</h2>
      <p style="margin-top:0;color:var(--muted);font-size:0.93rem;">
        Bu proje yerel Android (Kotlin + Jetpack Compose + Room + KSP) uygulamasıdır. Çevrimdışı ürün rehberleri, 7 günlük Room hava önbelleği, sesli okuma (TTS), sesli arama, foto/video bitki teşhisi ve canlı piyasa/haber takibi tek pakette sunulur.
      </p>
      <div class="row">${apkAction}</div>
    </section>

    <section class="card">
      <h2>🌦️ Hızlı Tarımsal Hava & İlaçlama Kontrolü (Web Önizleme)</h2>
      <div class="row">
        <select id="citySelect">
          <option value="37.7648,38.2786">Adıyaman Merkez</option>
          <option value="37.1591,38.7969">Şanlıurfa</option>
          <option value="37.0662,37.3833">Gaziantep</option>
          <option value="38.3552,38.3095">Malatya</option>
          <option value="37.0000,35.3213">Adana</option>
          <option value="37.8746,32.4932">Konya</option>
          <option value="38.4237,27.1428">İzmir</option>
          <option value="40.1885,29.0610">Bursa</option>
        </select>
        <button class="calc" onclick="loadPreviewWeather()">Hava Durumunu Getir</button>
      </div>
      <div id="weatherBox">Konum seçip “Hava Durumunu Getir” düğmesine dokunun.</div>
    </section>

    <section class="card">
      <h2>✨ Android Uygulama Özellikleri</h2>
      <div class="grid">
        <div class="feat">
          <strong>📈 Canlı Piyasa & Tarım Haberleri</strong>
          Buğday, arpa, Antep fıstığı, badem, zeytinyağı, kırmızı et, döviz, altın, 81 il akaryakıt, gübre bülteni ve tarım/hayvancılık haberleri.
        </div>
        <div class="feat">
          <strong>🔊 Sesli Okuma & Sesli Arama</strong>
          Hava durumu, yapay zeka raporu, piyasa özeti, haberler, ürün rehberi ve teşhis sonuçlarını Türkçe sesli okuma + eller serbest arama.
        </div>
        <div class="feat">
          <strong>📸 Foto / Video Teşhis</strong>
          Yaprak ve meyve görüntülerinde külleme, pas, mildiyö ve nekrotik leke analizi + hava zamanlaması.
        </div>
        <div class="feat">
          <strong>💾 Room 7 Günlük Önbellek</strong>
          İnternet kesildiğinde bile son 7 günün hava durumu ve FAO ET₀ tahminlerine çevrimdışı erişim.
        </div>
        <div class="feat">
          <strong>🌱 9 Ürün Görselli Atlas</strong>
          Bağ, Zeytin, Badem, Buğday, Arpa, Nohut, Nar, Elma ve Sebze için kapsamlı bakım rehberleri.
        </div>
      </div>
    </section>
  </main>

  <script>
    async function loadPreviewWeather() {
      const box = document.getElementById('weatherBox');
      const [lat, lon] = document.getElementById('citySelect').value.split(',');
      box.textContent = 'Open-Meteo verileri yükleniyor...';
      try {
        const res = await fetch(
          'https://api.open-meteo.com/v1/forecast?latitude=' + lat + '&longitude=' + lon +
          '&current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m&timezone=auto'
        );
        const data = await res.json();
        const c = data.current;
        let spray = '✅ İlaçlama için uygun pencere.';
        if (c.precipitation > 0.2) spray = '🚫 Yağış nedeniyle ilaçlama uygun değil.';
        else if (c.wind_speed_10m > 15) spray = '🚫 Rüzgâr yüksek (>15 km/s), drift riski var.';
        else if (c.temperature_2m > 30) spray = '⚠️ Sıcaklık yüksek (>30°C), buharlaşma/yanıklık riski.';
        box.innerHTML =
          '<strong>Sıcaklık:</strong> ' + c.temperature_2m + ' °C &nbsp;•&nbsp; ' +
          '<strong>Nem:</strong> %' + c.relative_humidity_2m + ' &nbsp;•&nbsp; ' +
          '<strong>Rüzgâr:</strong> ' + c.wind_speed_10m + ' km/s &nbsp;•&nbsp; ' +
          '<strong>Yağış:</strong> ' + c.precipitation + ' mm<br/>' +
          '<div style="margin-top:6px;font-weight:700;color:#1B5E20;">' + spray + '</div>';
      } catch (e) {
        box.textContent = 'Hava durumu verisi alınamadı.';
      }
    }
  </script>
</body>
</html>`;
}

const server = http.createServer((req, res) => {
  const urlPath = (req.url || '/').split('?')[0];

  if (urlPath === '/health' || urlPath === '/_health') {
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    if (req.method === 'HEAD') {
      res.end();
      return;
    }
    res.end(JSON.stringify({ status: 'ok', service: 'tarim-meteoroloji-ai' }));
    return;
  }

  if (urlPath === '/download/app-debug.apk' || urlPath === '/TarimMeteorolojiAI-v2.0.apk') {
    const apk = findExistingApk();
    if (!apk) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('APK bulunamadi. Android ortamiyla derleme gereklidir.');
      return;
    }
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': 'attachment; filename="TarimMeteorolojiAI-v2.0.apk"',
      'Content-Length': apk.sizeBytes,
      'Cache-Control': 'no-cache'
    });
    if (req.method === 'HEAD') {
      res.end();
      return;
    }
    fs.createReadStream(apk.filePath).pipe(res);
    return;
  }

  const apkInfo = findExistingApk();
  const html = renderHtml(apkInfo);
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache'
  });
  if (req.method === 'HEAD') {
    res.end();
    return;
  }
  res.end(html);
});

server.listen(PORT, HOST, () => {
  console.log(`Tarim Meteoroloji AI companion server listening on http://${HOST}:${PORT}`);
});
