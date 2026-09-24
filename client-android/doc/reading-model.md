# Reading model · 閱讀模型 · 阅读模型

Domain models and pagination live in `domain`; `data` stores Room records,
DataStore preferences and disk indexes. `format` and `pdf` parse local files.
`render` measures native text and renders PDF bitmaps; `ui` contains the
ViewModel and programmatic Android views.

領域模型與分頁位於 domain；data 保存 Room、DataStore 與磁碟索引。
format／pdf 解析本機文件；render 量測文字與繪製 PDF；ui 提供 ViewModel 與原生介面。

领域模型与分页位于 domain；data 保存 Room、DataStore 与磁盘索引。
format／pdf 解析本地文档；render 测量文字与绘制 PDF；ui 提供 ViewModel 与原生界面。

The block and page indexes are random-access files. Pagination runs on an IO
worker with native StaticLayout measurements. Continuous mode creates at most
five pages per visible pane. PDF bitmaps use a 32 MiB LRU per active document
and a per-bitmap pixel bound. Hidden panes release their bitmap caches.

內容與頁面索引為隨機存取檔案，原生文字量測與分頁在背景執行。
每個可見窗格最多建立五頁；PDF 使用 32 MiB LRU 與單張像素上限，隱藏窗格清除快取。

内容与页面索引为随机访问文件，原生文字测量与分页在后台执行。
每个可见窗格最多创建五页；PDF 使用 32 MiB LRU 与单张像素上限，隐藏窗格清除缓存。

Tab positions store page, pixel offset and logical block/character anchor.
Repagination locates the logical anchor; bookmarks/highlights remain attached
to their content. PDF pages retain their fixed page coordinates. Android 35+
uses platform PDF text APIs; older devices use the local PDF compatibility
parser while retaining native PdfRenderer rasterization.

分頁位置保存頁碼、像素捲動量及內容錨點；重排後依錨點定位。
PDF 保存固定頁面座標；新版使用平台文字 API，舊版以本機解析相容層搭配原生繪圖。

分页位置保存页码、像素滚动量及内容锚点；重排后按锚点定位。
PDF 保存固定页面坐标；新版使用平台文字 API，旧版以本地解析兼容层搭配原生绘图。
