# معرض البشيري للأجهزة الكهربائية — BESHEER COSMIC FUTURE
> **نظام الإدارة والمبيعات والمخازن الذكي بتقنية Jetpack Compose و Room Database**

---

## 🌟 نظرة عامة على المشروع (Project Overview)

تطبيق **«البشيري للأجهزة الكهربائية»** هو نظام إدارة متكامل ومخصص لمعارض ومستودعات الأجهزة الكهربائية والمنزلية والإلكترونية. يجمع التطبيق بين:
1. **قوة الإدارة التجارية الحقيقية:** إدارة المنتجات، الموديلات، المخزون، الفواتير متعددة الأصناف، كشوف حسابات العملاء والموردين وتجار الجملة، المرتجعات، الهالك، الخزينة، والمصروفات.
2. **الطباعة الحرارية:** دعم طباعة الفواتير على طابعات البلوتوث الحرارية القياسية (ESC/POS) بحجمي 80mm و 58mm بدقة أحادية اللون موفرة للحبر والورق.
3. **المساعد الصوتي الذكي «بشير»:** تنفيذ الأوامر بالصوت (بحث أسعار، استعلام عن رصيد الخزينة، تسجيل المصروفات، بدء فواتير البيع، والتنقل).
4. **الهوية البصرية الفضائية الفاخرة (BESHEER COSMIC FUTURE):** واجهة نظام شمسي ثلاثية الأبعاد تفاعلية، انتقال سينمائي سلس، وكواكب تمثل أقسام التطبيق مع كوكب المنتجات المستوحى من الأرض المستقبلية.

---

## 🛠 متطلبات بيئة التطوير (Development Prerequisites)

- **نظام التشغيل:** Windows 10/11، macOS، أو Linux.
- **بيئة التطوير (IDE):** [Android Studio Ladybug (2024.2+)](https://developer.android.com/studio) أو أحدث.
- **حزمة تطوير جافا (JDK):** OpenJDK 17 أو OpenJDK 21 (موصى بـ JDK 17 المتوافق مع AGP 8.7).
- **إصدار أندرويد المستهدف:**
  - `compileSdk`: **35** (Android 15)
  - `targetSdk`: **35**
  - `minSdk`: **26** (Android 8.0 Oreo وما فوق — متوافق مع أكثر من 96% من الهواتف).
- **أداة البناء (Build Tool):** Gradle 8.9+ مع Kotlin 2.0.20 و KSP (Kotlin Symbol Processing).

---

## 🚀 خطوات فتح المشروع وبنائه (Build & Run Instructions)

### 1. فتح المشروع داخل Android Studio
1. قم بفك ضغط ملف الكود المصدري `AlBashiri_SourceCode_Complete.zip`.
2. افتح **Android Studio** واختر **Open** ثم حدد المجلد الرئيسي للمشروع.
3. انتظر اكتمال عملية مزامنة Gradle (Sync Project with Gradle Files).
4. تأكد من ضبط Gradle JDK من خلال:
   `Settings/Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK: Embedded JDK 17`.

### 2. بناء المشروع عبر سطر الأوامر (Command Line)
إذا كان Gradle مثبتاً في نظامك:
```bash
# تجميع نسخة التطبيق (Debug APK)
gradle :app:assembleDebug

# تشغيل اختبارات الوحدات (Unit & Robolectric Tests)
gradle :app:testDebugUnitTest
```
سيتم توليد ملف الـ APK في المسار:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. تشغيل التطبيق على الهاتف أو المحاكي
1. قم بتوصيل هاتف Android عبر كابل USB مع تفعيل **تصحيح أخطاء USB (USB Debugging)**، أو أنشئ جهاز محاكي (AVD) بنظام Android 10+ (API 29+).
2. اضغط على زر **Run 'app'** (`Shift + F10`) في شريط أدوات Android Studio.

---

## 🏗 البنية الهندسية للمشروع (Architecture Overview)

يتبع التطبيق أحدث معايير **Android Architecture Components** و **Jetpack Compose Clean MVVM**:

```
app/src/main/java/com/example/
├── MainActivity.kt                      # نقطة الدخول الرئيسية، وتوجيه الشاشات، وإدارة الـ Scaffold
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              # قاعدة بيانات Room SQLite المحلية (v14)
│   │   ├── AppDao.kt                   # دوال الاستعلام والعمليات (Queries, Inserts, Updates, Soft Deletes)
│   │   ├── Entities.kt                 # جداول قاعدة البيانات (Categories, Products, Models, Sales, Invoices, Accounts, ...)
│   │   └── DatabaseInitializer.kt      # التهيئة التلقائية مع بيانات المعرض المبدئية وحسابات المستخدمين
│   └── repository/
│       └── StoreRepository.kt          # مستودع إدارة البيانات والعمليات المتزامنة والتدفقات (Flows)
├── ui/
│   ├── MainViewModel.kt                # إدارة الحالة المركزية (StateFlow) لجميع شاشات التطبيق
│   ├── components/
│   │   ├── StoreTopAppBar.kt           # الشريط العلوي مع مراعاة منطقة الأمان والشعار والإشعارات
│   │   ├── BasheerVoiceDialog.kt       # نافذة المساعد الصوتي بشير بالنواة المغناطيسية الفضائية
│   │   ├── InvoicePreviewDialog.kt     # معاينة وطباعة الفاتورة الحرارية (Monochrome 100% White/Black)
│   │   ├── SaleDialog.kt               # نافذة تسجيل البيع السريع وخصم المخزون
│   │   ├── AdminDialogs.kt             # نوافذ إضافة وتعديل الأقسام والماركات والمنتجات والموديلات
│   │   ├── PrinterDialogs.kt           # نافذة اختيار وإعداد طابعة البلوتوث الحرارية
│   │   └── FutureGateDecorations.kt    # مكونات التصميم الزجاجي والهالات والجسيمات الضوئية
│   ├── screens/
│   │   ├── cosmic/
│   │   │   ├── CosmicSolarSystemScreen.kt          # شاشة النظام الشمسي والمدارات الكوكبية
│   │   │   ├── CosmicProductsPlanetScreen.kt       # كوكب المنتجات المستوحى من الأرض المستقبلية
│   │   │   └── CosmicPlanetaryTransitionOverlay.kt # محرك الانتقال السينمائي ثلاثي الأبعاد
│   │   ├── LoginScreen.kt              # بوابة الدخول واختيار الحسابات
│   │   ├── CatalogScreen.kt            # لوحة التحكم وإدارة المعرض
│   │   ├── InventoryScreen.kt          # إدارة المخزون، الجرد الفعلي، وحركة الأصناف
│   │   ├── CreateInvoiceScreen.kt      # إنشاء الفواتير متعددة الأصناف مع الخصم والمدفوع
│   │   ├── SalesLogScreen.kt           # سجل الفواتير والمبيعات وإعادة الطباعة والمرتجعات
│   │   ├── AccountsScreen.kt           # العملاء، الموردون، تجار الجملة، المشتريات، الخزينة، والتقارير
│   │   ├── ReturnsScreen.kt            # إدارة المرتجعات واسترداد الكميات
│   │   ├── DamageRepairScreen.kt       # الهالك والتالف والإصلاحات الخارجية
│   │   ├── SettingsScreen.kt           # بيانات المعرض، الطابعة، والنسخ الاحتياطي
│   │   └── RecycleBinScreen.kt         # سلة المحذوفات القابلة للاستعادة
│   ├── theme/
│   │   ├── Color.kt                    # نظام الألوان والتدرجات الذهبية والكحلية والسيان
│   │   ├── Theme.kt                    # ثيم Material Design 3 الداكن الفاخر
│   │   └── Type.kt                     # خط Cairo العربي الأصيل
│   └── voice/
│       ├── BasheerVoiceParser.kt       # محلل الأوامر الصوتية باللغة العربية
│       ├── SpeechRecognizerHelper.kt   # مساعد التعرف على الصوت عبر أندرويد
│       └── TtsHelper.kt                # ناطق الردود الصوتية (Text-To-Speech)
└── utils/
    ├── BluetoothPrinterHelper.kt       # محرك طباعة ESC/POS الحرارية
    ├── StoreProfileManager.kt          # إدارة ملف وإعدادات المتجر
    ├── RecycleBinSerializer.kt         # أرشفة وحفظ بيانات سلة المحذوفات
    └── WhatsAppHelper.kt               # مشاركة الفواتير والتقارير عبر واتساب
```

---

## 🔑 الحسابات والصلاحيات الافتراضية (Default Credentials)

التطبيق مهيأ مسبقاً بأربعة حسابات جاهزة للاستخدام:
- **حساب المدير (ADMIN):**
  - اسم المستخدم: `مدير النظام` (المعرف: `admin`)
  - رمز المرور (PIN): `1234`
  - الصلاحيات: كاملة (إضافة، تعديل، حذف، تقارير، خزينة، أرباح، جرد، نسخ احتياطي).
- **حسابات البائعين (SELLER):**
  - البائع 1: المعرف `seller1` — رمز المرور `1111`
  - البائع 2: المعرف `seller2` — رمز المرور `2222`
  - البائع 3: المعرف `seller3` — رمز المرور `3333`
  - الصلاحيات: مبيعات، استخراج فواتير، استعلام عن الأسعار والمخزون، مع حجب تكاليف الشراء وأرباح المعرض.

---

## 📄 توثيق Codex الإضافي
يرجى قراءة الملف التفصيلي **`CODEX_HANDOVER_SPECIFICATION.md`** المرفق داخل المشروع للحصول على الدليل البرمجي الكامل والخرائط التفصيلية لكل شاشة وكيانات قاعدة البيانات.
