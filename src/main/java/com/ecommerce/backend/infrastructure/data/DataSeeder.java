package com.ecommerce.backend.infrastructure.data;

import com.ecommerce.backend.domain.entity.Campaign;
import com.ecommerce.backend.domain.entity.Category;
import com.ecommerce.backend.domain.entity.Product;
import com.ecommerce.backend.domain.entity.Review;
import com.ecommerce.backend.domain.entity.User;
import com.ecommerce.backend.infrastructure.repository.CampaignRepository;
import com.ecommerce.backend.infrastructure.repository.CategoryRepository;
import com.ecommerce.backend.infrastructure.repository.ProductRepository;
import com.ecommerce.backend.infrastructure.repository.ReviewRepository;
import com.ecommerce.backend.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    private static final String[] REVIEW_TITLES_TR = {
            "Çok memnun kaldım", "Kargo süper hızlı", "Fiyat performans şahane", "Kalite beklediğimden iyi",
            "Günlük kullanıma tam uygun", "Paketleme özenliydi", "Tavsiye ederim", "İndirimden şansımıza denk geldik",
            "Ürün orijinal hissettiriyor", "Ailecek beğendik", "Tekrar sipariş vereceğim", "Fotoğrafla aynı",
            "Orta segment için doğru", "Biraz gecikti ama sorun değil", "Kurulum kolaydı",
            "Müşteri hizmetleri ilgili", "Garanti belgesi kutuda vardı", "Ofiste kullanıyorum, memnunum",
            "Çocuk çok sevdi", "Kıyasladım en uygun buradaydı", "Hafif çizik vardı, genel olarak OK",
            "Beklentimin altında", "İade düşünüyorum", "Kargo hasarlı geldi", "Ürün güzel, fiyat yüksek",
            "Şüpheyle aldım, iyiki almışım", "Yorumlara güvenip aldım, doğru çıktı"
    };

    private static final String[] REVIEW_COMMENTS_POS = {
            "Ürün açıklamasıyla uyumlu. Satıcıya ve kargoya teşekkürler.",
            "Hızlı teslimat, hasarsız paket. Performansından memnunum.",
            "Fiyatına göre kalite gayet iyi; arkadaşlarıma da önerdim.",
            "Uzun süredir kullanıyorum, dayanıklı ve kullanışlı çıktı.",
            "Kampanyalı fiyattan aldım, pişman değilim.",
            "Küçük detaylar bile düşünülmüş, memnun kaldık.",
            "Türkiye şartlarında fiyat artmış olsa da değer buldum.",
            "Resmi satış gibi; fatura ve garanti sorunsuz."
    };

    private static final String[] REVIEW_COMMENTS_MID = {
            "İdare eder düzeyinde; fiyat biraz daha uygun olabilirdi.",
            "Beklentimin bir tık altında kaldı ama kullanılıyor.",
            "Genel olarak OK; kullanım kılavuzu kısa kalmış.",
            "Orta kalite, günlük iş görür.",
            "Kargo bir gün gecikti, ürün sağlamdı."
    };

    private static final String[] REVIEW_COMMENTS_NEG = {
            "Ürün hasarlı geldi, iade sürecine başvuracağım.",
            "Açıklamayla uyuşmadı; beklentimi karşılamadı.",
            "Kalite düşük geldi, tavsiye etmem.",
            "Kargo firması ile iletişim sorunu yaşadım."
    };

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            seedCategories();
        }
        if (productRepository.count() == 0) {
            seedProducts();
        }
        if (campaignRepository.count() == 0) {
            seedCampaigns();
        }
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (reviewRepository.count() == 0) {
            seedReviews();
        }
    }

    private void seedCategories() {
        List<Category> categories = Arrays.asList(
                new Category("Elektronik", "Telefon, bilgisayar, TV ve aksesuar",
                        "https://picsum.photos/seed/elk/200/200"),
                new Category("Giyim", "Kadın, erkek ve çocuk modası", "https://picsum.photos/seed/giy/200/200"),
                new Category("Ev & Yaşam", "Mobilya, mutfak, dekorasyon", "https://picsum.photos/seed/ev/200/200"),
                new Category("Spor", "Fitness, outdoor, takım sporları", "https://picsum.photos/seed/spo/200/200"),
                new Category("Kitap", "Roman, eğitim, çocuk kitapları", "https://picsum.photos/seed/kit/200/200"),
                new Category("Kozmetik", "Cilt bakımı, makyaj, parfüm", "https://picsum.photos/seed/koz/200/200"),
                new Category("Otomotiv", "Lastik, bakım, aksesuar", "https://picsum.photos/seed/oto/200/200"),
                new Category("Oyuncak", "Lego, bebek oyuncakları, hobi", "https://picsum.photos/seed/oyn/200/200"),
                new Category("Müzik", "Enstrüman, kulaklık, hoparlör", "https://picsum.photos/seed/muz/200/200"),
                new Category("Bahçe", "El aletleri, sulama, tohum", "https://picsum.photos/seed/bah/200/200"),
                new Category("Ofis", "Kırtasiye, mobilya, teknoloji", "https://picsum.photos/seed/ofi/200/200"),
                new Category("Seyahat", "Valiz, çanta, aksesuar", "https://picsum.photos/seed/sey/200/200"),
                new Category("Sağlık", "Vitamin, medikal, bakım", "https://picsum.photos/seed/sag/200/200"),
                new Category("Pet Shop", "Mama, oyuncak, bakım", "https://picsum.photos/seed/pet/200/200"),
                new Category("Hobi", "Sanat, model, koleksiyon", "https://picsum.photos/seed/hob/200/200"));
        categoryRepository.saveAll(categories);
    }

    private void seedProducts() {
        List<Category> categories = categoryRepository.findAll();

        List<List<String>> turkishByCategory = Arrays.asList(
                Arrays.asList(
                        "Samsung Galaxy A54 5G 128 GB", "Apple iPhone 15 128 GB", "Xiaomi Redmi Note 13 Pro 256 GB",
                        "MacBook Air M2 256 GB", "Lenovo IdeaPad Slim 5 i5", "LG 55'' 4K UHD Smart TV",
                        "Sony WH-1000XM5 Kulaklık", "JBL Flip 6 Bluetooth Hoparlör", "Apple AirPods Pro 2",
                        "PlayStation 5 Dijital Edition", "DJI Mini 3 Drone", "Canon EOS R50 Fotoğraf Makinesi",
                        "iPad 10. Nesil 64 GB", "Samsung Galaxy Tab S9", "Garmin Forerunner 265 Akıllı Saat",
                        "Dyson V15 Süpürge", "Philips Airfryer XXL", "Bosch Çamaşır Makinesi 9 kg",
                        "Arçelik No-Frost Buzdolabı", "Vestel Ankastre Fırın", "Huawei MatePad 11.5 Tablet",
                        "Logitech MX Master 3S Mouse", "Casper Excalibur Oyuncu Laptopu", "Rampage Mekanik Klavye",
                        "Anker Power Bank 20000 mAh", "TP-Link Mesh WiFi 6 Sistem", "GoPro Hero 12 Black"),
                Arrays.asList(
                        "Nike Air Max 270 Erkek Spor Ayakkabı", "Adidas Ultraboost 22 Koşu Ayakkabısı",
                        "Levi's 501 Original Kot Pantolon", "Koton Oversize Kadın Tişört", "Mavi Slim Fit Erkek Gömlek",
                        "LC Waikiki Polar Mont", "Defacto Çocuk Eşofman Takımı", "Puma RS-X Sneaker",
                        "The North Face Outdoor Mont", "Columbia Trekking Botu", "US Polo Kadın Çanta",
                        "Pierre Cardin Erkek Kemer", "Koton Mom Jean", "Mavi Kadın Jean Şort",
                        "Nike Dri-FIT Antrenman Tişörtü", "Adidas Şort Basketbol", "Lacoste Polo Yaka Tişört",
                        "Tommy Hilfiger Denim Ceket", "H&M Kaşmir Karışımlı Kazak", "Zara Deri Ceket",
                        "Pull&Bear Oversize Sweatshirt", "Stradivarius Midi Etek", "Mango Blazer Ceket",
                        "Kiğılı Takım Elbise", "Damat Slim Fit Gömlek", "Colin's Slim Chino Pantolon"),
                Arrays.asList(
                        "IKEA MALM 160x200 Karyola", "Karaca Biogranit 7 Parça Tencere", "English Home Nevresim Takımı",
                        "Taç Saten Çift Kişilik Pike", "Jumbo Yatak Odası Halısı", "Emsan Çelik Çaydanlık",
                        "Korkmaz Granit Tava Seti", "Tefal Titanium Tencere", "Philips Hue Akıllı Ampul Seti",
                        "Fakir Elektrikli Süpürge", "Arzum Okka Türk Kahvesi Makinesi", "Sinbo Çay Makinesi",
                        "Karaca Fine Pearl Yemek Takımı", "Paşabahçe Meşrubat Bardağı Seti",
                        "Madame Coco Dekoratif Yastık",
                        "Bella Maison Runner", "Linens Banyo Havlu Seti", "English Home Mutfak Önlüğü",
                        "Stanley Termos 1.18 L", "Kütahya Porselen Kahve Fincanı", "Emsan Çay Bardağı 6'lı",
                        "İkea Lack Sehpa", "Çok Amaçlı Depolama Sepeti", "Black+Decker Matkap Seti",
                        "Gardena Hortum Seti 20 m", "Karcher Basınçlı Yıkama"),
                Arrays.asList(
                        "Nike Basketbol Topu", "Adidas Futbol Topu", "Wilson Tenis Raketi",
                        "Lululemon Yoga Matı", "Peloton Bisiklet Aksesuarı", "Bowflex Ayarlanabilir Dambıl",
                        "Under Armour Sıkıştırma Tişört", "Columbia Trekking Batonu", "Merrell Outdoor Ayakkabı",
                        "Salomon Trail Koşu Ayakkabısı", "Garmin Forerunner Saat", "Suunto Pusula",
                        "Theraband Direnç Lastiği", "Concept2 Kürek Makinesi Aksesuarı", "NordicTrack Koşu Bandı Matı",
                        "Everlast Boks Eldiveni", "Head Tenis Çantası", "Speedo Yüzücü Gözlüğü",
                        "Arena Mayo Erkek", "Decathlon Kamp Sandalyesi", "Quechua 40 L Sırt Çantası",
                        "Hydro Flask Matara 1 L", "Trigger Point Masaj Topu", "Foam Roller 90 cm",
                        "Polar Nabız Kemeri", "Fitbit Charge 6"),
                Arrays.asList(
                        "Sabahattin Ali — Kürk Mantolu Madonna", "Orhan Pamuk — Kar", "Yuval Noah Harari — Sapiens",
                        "George Orwell — 1984", "Stefan Zweig — Satranç", "José Saramago — Körlük",
                        "Dan Brown — Cehennem", "Agatha Christie — On Kişiydiler", "J.K. Rowling — Harry Potter Set",
                        "İhsan Oktay Anar — Puslu Kıtalar Atlası", "Oğuz Atay — Tutunamayanlar",
                        "Reşat Nuri — Çalıkuşu",
                        "Paulo Coelho — Simyacı", "Haruki Murakami — 1Q84", "Isaac Asimov — Vakıf Üçlemesi",
                        "Frank Herbert — Dune", "Brandon Sanderson — Mistborn", "Stephen King — IT",
                        "Neil Gaiman — Amerikan Tanrıları", "Tolkien — Yüzüklerin Efendisi Kutulu Set",
                        "Çocuklar İçin Boyama Kitabı", "TYT Matematik Soru Bankası", "KPSS Genel Yetenek",
                        "İngilizce Gramer Pratik", "Python ile Veri Bilimi", "Clean Code (Türkçe)"),
                Arrays.asList(
                        "L'Oréal Paris Nem Terapisi Krem", "Maybelline Sky High Maskara", "Flormar Mat Ruj Seti",
                        "Note BB Krem", "Golden Rose Likit Ruj", "The Ordinary Niacinamide Serum",
                        "La Roche-Posay Effaclar Temizleyici", "Bioderma Sensibio Misel Su",
                        "CeraVe Nemlendirici Losyon",
                        "Nivea Güneş Kremi SPF 50", "Garnier Micellar Temizleme Suyu", "Avon Luck Kadın Parfümü",
                        "Calvin Klein CK One", "Paco Rabanne 1 Million", "Chanel Coco Mademoiselle (tester)",
                        "MAC Studio Fix Fondöten", "Sephora Fırça Seti", "Real Techniques Sünger",
                        "Urban Decay Naked Palette", "Benefit Kaş Jeli", "Clinique Nem Dengesi Losyon",
                        "Estée Lauder Gece Onarım Serumu", "Dior Sauvage Erkek Parfüm", "Gillette Fusion Yedek Bıçak",
                        "Oral-B Şarjlı Diş Fırçası", "Schwarzkopf Saç Boyası"));

        for (int i = 0; i < turkishByCategory.size() && i < categories.size(); i++) {
            Category category = categories.get(i);
            int seq = 0;
            for (String productName : turkishByCategory.get(i)) {
                saveProduct(category, productName, seq++);
            }
        }

        for (int i = turkishByCategory.size(); i < categories.size(); i++) {
            Category category = categories.get(i);
            List<String> extras = extraNamesForCategory(category.getCategoryName());
            int seq = 0;
            for (String productName : extras) {
                saveProduct(category, productName, seq++);
            }
        }

        for (Category category : categories) {
            for (int i = 0; i < 18; i++) {
                String name = category.getCategoryName() + " — seçili ürün " + (i + 1);
                saveProduct(category, name, 100 + i);
            }
        }
    }

    private List<String> extraNamesForCategory(String categoryName) {
        return switch (categoryName) {
            case "Otomotiv" -> Arrays.asList(
                    "Michelin 205/55 R16 4 Mevsim Lastik", "Castrol Edge 5W-30 Motor Yağı 4 L",
                    "Karcher Oto Şampuanı", "Osram Far Ampulü H7", "Bosch Silecek Takımı",
                    "Thule Tavan Bagaj Ahtapotu", "Baseus Araç Telefon Tutucu", "TomTom Navigasyon Cihazı",
                    "Blackvue Dashcam", "Osram LED İç Aydınlatma", "Goodyear EfficientGrip", "Liqui Moly Motor Katkısı",
                    "Meguiar's Cila Seti", "3M Bant ve Tamir", "Castrol Transmax Şanzıman Yağı",
                    "Castrol Brake Fluid", "Bosch Akü 60 Ah", "Varta Akü 72 Ah",
                    "Fakir Oto Süpürgesi", "Stanley Oto Çakı Seti", "Noco Akü Takviye Cihazı",
                    "Garmin Oto Navigasyon", "Nextbase Araç Kamerası", "TP-Link Araç WiFi",
                    "Philips Xenon Ampul", "Osram LED Sis Farı Ampulü");
            case "Oyuncak" -> Arrays.asList(
                    "Lego Technic Yarış Arabası", "Barbie Dreamhouse", "Play-Doh Büyük Set",
                    "Hot Wheels 20'li Garaj", "Monopoly Türkiye", "Jenga Klasik",
                    "Nerf Elite Blaster", "Baby Alive Bebek", "Fisher-Price Piano Mat",
                    "K'nex Roller Coaster", "Ravensburger Puzzle 1000", "Playmobil Hayvanat Bahçesi",
                    "Transformers Figür", "Star Wars Lego X-Wing", "Disney Prenses Seti",
                    "Scrabble Türkçe", "Uno Kart Oyunu", "Tabu Aile Oyunu",
                    "Bilardo Mini Masa", "Dart Tahtası Manyetik", "Yüzme Havuzu 3 m",
                    "Kum Havuzu Seti", "Trambolin 140 cm", "Scooter LED Işıklı",
                    "Bisiklet 20 Jant Çocuk", "Paten Inline 4 Teker");
            case "Müzik" -> Arrays.asList(
                    "Yamaha P-45 Dijital Piyano", "Casio CT-S300 Org", "Fender Squier Elektro Gitar",
                    "Ibanez RG Gitar", "Marshall MG15 Amfi", "Boss Katana Mini Amfi",
                    "Shure SM58 Mikrofon", "Audio-Technica AT2020", "Focusrite Scarlett 2i2",
                    "Akai MPK Mini Klavye", "Roland TD-1K Davul Seti", "Pearl Roadshow Akustik Davul",
                    "Hohner Blues Harmonica", "Yamaha Tenor Saksafon Ağızlık", "Kawai CA49 Piyano",
                    "Behringer Ses Mikseri", "JBL EON 612 Hoparlör", "Pioneer DJ Controller",
                    "Numark DJ Seti", "Korg Volca Beats", "Teenage Engineering OP-1",
                    "Moog Subsequent 25", "Rode VideoMic Pro", "Zoom H6 Kayıt Cihazı",
                    "Sennheiser HD 560S", "Beyerdynamic DT 770 Pro");
            case "Bahçe" -> Arrays.asList(
                    "Gardena Budama Makası", "Fiskars Kazma", "Oleo-Mac Çim Biçme Makinesi",
                    "Stihl Tırpan", "Husqvarna Zincirli Testere", "Karcher Bahçe Hortumu 30 m",
                    "Yaprak Toplama Tırmığı", "Serinova Sulama Zamanlayıcı", "Tohum Paketi Domates",
                    "Saksı Seti 3'lü", "Toprak 50 L Torba", "Gübre Organik 10 kg",
                    "Compost Kovası", "Sera Mini Polikarbon", "Çim Tohumu 1 kg",
                    "Çit Teli Galvaniz", "Bahçe Lambası Solar", "Dekoratif Çeşme",
                    "Şemsiye 3 m Gölgelik", "Hammock Askılı", "Barbekü Kömürlü",
                    "Mangal Seti Taşınabilir", "Bahçe Mobilyası Rattan", "Şezlong Katlanır",
                    "Peyzaj Aydınlatma LED", "Damlama Sulama Seti");
            case "Ofis" -> Arrays.asList(
                    "Herman Miller Aeron Koltuk", "IKEA Markus Ofis Koltuğu", "Dell UltraSharp 27 Monitör",
                    "LG 24'' IPS Monitör", "Logitech MX Keys Klavye", "SteelSeries Apex Pro",
                    "Samsung T7 SSD 1 TB", "WD Elements HDD 4 TB", "Canon Lazer Yazıcı",
                    "Epson Tanklı Yazıcı", "Fellowes Evrak İmha", "Leitz Klasör Seti",
                    "Faber-Castell Kalem Seti", "Moleskine Defter A5", "3M Post-it Büyük Paket",
                    "Tükenmez Kalem 50'li", "Beyaz Tahta Seti", "Projeksiyon Perdesi",
                    "Kablosuz Sunum Kumandası", "Webcam 4K", "Yealink Konferans Telefonu",
                    "Jabra Evolve Kulaklık", "Anker USB-C Hub", "Ugreen Kablo Organizer",
                    "Ergotron Monitör Kolu", "Footrest Ayarlanabilir");
            case "Seyahat" -> Arrays.asList(
                    "Samsonite Cosmolite Valiz 75 cm", "American Tourister Kabin Boy", "Eastpak Sırt Çantası 30 L",
                    "The North Face Sırt Çantası 40 L", "Osprey Hiking Çantası", "Victorinox Çakı",
                    "Go Travel Boyun Yastığı", "Bose QC Kulaklık Seyahat", "Anker Seyahat Adaptörü",
                    "Xiaomi Power Bank 10000", "Su Geçirmez Çanta Kılıfı", "Pasaportluk RFID",
                    "Bagaj Tartısı Dijital", "Kilit TSA Onaylı", "Organizer Çanta İçi",
                    "Ayakkabı Torbası", "Katlanır Su Şişesi", "Microfiber Havlu",
                    "Güneş Gözlüğü Polarize", "Şapka Katlanır", "Yağmurluk Ceket Cepli",
                    "Bavul Kemeri Çapraz", "Valiz Etiketi Bluetooth", "Seyahat Boy Şişe Seti",
                    "Uyku Maskesi 3D", "Kulak Tıkacı Köpük");
            case "Sağlık" -> Arrays.asList(
                    "Omron Tansiyon Aleti", "Beurer Termometre", "Philips Buhar Makinesi",
                    "Braun Iradyan Ateş Ölçer", "Solgar Multivitamin", "Ocean Vitamin D3 Damla",
                    "Imodium Kapsül", "Aspirin Protect", "Theraflu Saşe",
                    "Sterimar Burun Spreyi", "Otrivin Sprey", "Corega Tablet",
                    "Listerine Ağız Bakım Suyu", "Sensodyne Diş Macunu", "Oral-B Diş İpi",
                    "Ebselen Omega 3", "Supradyn Tablet", "Ocean Magnesium",
                    "Hidrofil Pamuk 500 g", "Yara Bandı Su Geçirmez", "Maske Cerrahi 50'li",
                    "Dezenfektan Sprey 1 L", "El Losyonu 500 ml", "Dijital Baskül",
                    "Nemlendirici Hava Cihazı", "Hava Temizleyici HEPA");
            case "Pet Shop" -> Arrays.asList(
                    "Royal Canin Kedi Maması 4 kg", "Pro Plan Köpek Maması 14 kg", "Whiskas Pouch 12'li",
                    "Pedigree Köpek Ödülü", "Trixie Tüy Toplayıcı", "Karlie Tasma Seti",
                    "Ferplast Kedi Tuvaleti", "Catit Su Pınarı", "Kong Classic Oyuncak",
                    "Trixie Tırmalama Tahtası", "Akvaryum 60 L Set", "Balık Yemi Flake",
                    "Kuş Yemi Premium", "Kemirgen Talaşı 15 L", "Kafes Papağan Orta Boy",
                    "Köpek Yatağı Ortopedik", "Kedi Evi Karton", "Tasma LED Işıklı",
                    "Tırnak Makası Pet", "Şampuan Köpek Hassas", "Diş Fırçası Köpek",
                    "Tasma Otomatik 5 m", "Taşıma Çantası Kedi", "Yelek Köpek Reflektörlü",
                    "Oyuncak Sesli Köpek", "Kedi Otu 100 g");
            case "Hobi" -> Arrays.asList(
                    "Revell Model Uçak 1/72", "Tamiya Model Tank", "Airfix Başlangıç Seti",
                    "Vallejo Boya Seti 16 Renk", "Pense Modelci", "Olfa Maket Bıçağı",
                    "Tamiya Yapıştırıcı", "Zımpara Seti Çoklu", "Ahşap Boyama Tuval 40x50",
                    "Akrilik Boya 12 Renk", "Fırça Seti Sentetik", "Şövale Taşınabilir",
                    "Origami Kağıt 500 Yaprak", "Boncuk Seti Renkli", "Örgü İpi Yün 5'li",
                    "Tığ Seti Metal", "Şiş Örgü Seti", "Dikiş Makinesi Mini",
                    "Etamin Kiti Çiçek", "Diamond Painting 40x50", "Puzzle 3000 Parça",
                    "Rubik Küp 3x3 Hızlı", "Satranç Taşları Ahşap", "Go Tahtası Katlanır",
                    "Koleksiyonluk Figür", "Funko Pop Seçili Karakter");
            default -> Arrays.asList(
                    categoryName + " — öne çıkan model A", categoryName + " — öne çıkan model B",
                    categoryName + " — öne çıkan model C", categoryName + " — öne çıkan model D",
                    categoryName + " — öne çıkan model E", categoryName + " — öne çıkan model F",
                    categoryName + " — öne çıkan model G", categoryName + " — öne çıkan model H",
                    categoryName + " — öne çıkan model I", categoryName + " — öne çıkan model J",
                    categoryName + " — öne çıkan model K", categoryName + " — öne çıkan model L",
                    categoryName + " — öne çıkan model M", categoryName + " — öne çıkan model N",
                    categoryName + " — öne çıkan model O", categoryName + " — öne çıkan model P",
                    categoryName + " — öne çıkan model Q", categoryName + " — öne çıkan model R",
                    categoryName + " — öne çıkan model S", categoryName + " — öne çıkan model T",
                    categoryName + " — öne çıkan model U", categoryName + " — öne çıkan model V",
                    categoryName + " — öne çıkan model W", categoryName + " — öne çıkan model X",
                    categoryName + " — öne çıkan model Y", categoryName + " — öne çıkan model Z");
        };
    }

    private void saveProduct(Category category, String productName, int seq) {
        Product product = new Product();
        product.setProductName(productName);
        product.setUnitPrice(priceForCategory(category, seq));
        product.setUnitInStock(5 + random.nextInt(180));
        product.setQuantityPerUnit("1 adet");
        product.setCategory(category);
        product.setDescription(descriptionFor(category.getCategoryName(), productName));
        product.setImageUrl("https://picsum.photos/seed/p" + Math.abs(productName.hashCode() % 10000) + "/400/400");
        int d = random.nextInt(100);
        product.setDiscount(d < 55 ? 5 + random.nextInt(40) : 0);
        product.setIsActive(true);
        productRepository.save(product);
    }

    private BigDecimal priceForCategory(Category category, int seq) {
        String n = category.getCategoryName();
        double base;
        double spread;
        if (n.contains("Elektronik")) {
            base = 2_499;
            spread = 92_000;
        } else if (n.contains("Giyim")) {
            base = 149;
            spread = 8_500;
        } else if (n.contains("Ev")) {
            base = 199;
            spread = 42_000;
        } else if (n.contains("Spor")) {
            base = 249;
            spread = 18_000;
        } else if (n.contains("Kitap")) {
            base = 79;
            spread = 1_200;
        } else if (n.contains("Kozmetik")) {
            base = 59;
            spread = 4_200;
        } else if (n.contains("Otomotiv")) {
            base = 299;
            spread = 28_000;
        } else if (n.contains("Oyuncak")) {
            base = 99;
            spread = 9_000;
        } else if (n.contains("Müzik")) {
            base = 399;
            spread = 65_000;
        } else if (n.contains("Bahçe")) {
            base = 89;
            spread = 12_000;
        } else if (n.contains("Ofis")) {
            base = 49;
            spread = 35_000;
        } else if (n.contains("Seyahat")) {
            base = 199;
            spread = 8_500;
        } else if (n.contains("Sağlık")) {
            base = 39;
            spread = 3_800;
        } else if (n.contains("Pet")) {
            base = 79;
            spread = 6_500;
        } else if (n.contains("Hobi")) {
            base = 59;
            spread = 7_500;
        } else {
            base = 99;
            spread = 5_000;
        }
        double jitter = (seq % 7) * (spread / 120.0);
        double v = base + random.nextDouble() * spread * 0.85 + jitter;
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private String descriptionFor(String categoryName, String productName) {
        return switch (categoryName) {
            case "Elektronik" -> "Garantili, faturalı; hızlı kargo. " + productName
                    + " için teknik özellikleri ürün kartında inceleyin.";
            case "Giyim" -> "Beden tablosuna uyun. " + productName + " — günlük kullanım ve kombin için uygun.";
            case "Ev & Yaşam" -> "Ev ve mutfak kullanımına uygun. " + productName + " kolay temizlenebilir yapıda.";
            default -> "KapıdaMart demo vitrin ürünü. " + productName + " — stok ve fiyat bilgisi örnektir.";
        };
    }

    private void seedCampaigns() {
        List<Campaign> campaigns = Arrays.asList(
                new Campaign(
                        "Büyük İndirim Fırsatı",
                        "Sepet dolu, fiyat yarı yarıya",
                        "Seçili kategorilerde ekstra indirim kuponlarıyla birleştirilebilir.",
                        50,
                        "https://picsum.photos/seed/c1/800/400",
                        "#FF6B6B",
                        "3 gün kaldı",
                        "Alışverişe Başla",
                        "/shop",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(3)),
                new Campaign(
                        "Elektronik Günleri",
                        "Telefon, TV, küçük ev aletleri",
                        "Yeni sezon modellerde kampanya fiyatları.",
                        32,
                        "https://picsum.photos/seed/c2/800/400",
                        "#4ECDC4",
                        "1 hafta",
                        "Elektronik",
                        "/shop?categoryId=1",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)),
                new Campaign(
                        "Yaz Koleksiyonu",
                        "Giyim ve ayakkabı",
                        "Hafif kumaşlar ve outdoor seçenekleri.",
                        28,
                        "https://picsum.photos/seed/c3/800/400",
                        "#45B7D1",
                        "2 hafta",
                        "Giyim",
                        "/shop?categoryId=2",
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().plusDays(12)),
                new Campaign(
                        "Ev & yaşam haftası",
                        "Mutfak ve dekorasyon",
                        "Tencere takımlarından halıya geniş ürün yelpazesi.",
                        38,
                        "https://picsum.photos/seed/c4/800/400",
                        "#96CEB4",
                        "5 gün",
                        "Keşfet",
                        "/shop?categoryId=3",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(4)),
                new Campaign(
                        "Spor & outdoor",
                        "Sağlıklı yaşam",
                        "Fitness ve doğa sporları ekipmanlarında indirim.",
                        35,
                        "https://picsum.photos/seed/c5/800/400",
                        "#FFEAA7",
                        "1 hafta",
                        "Spor",
                        "/shop?categoryId=4",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)));
        campaignRepository.saveAll(campaigns);
    }

    private void seedUsers() {
        String pw = passwordEncoder.encode("user123");
        List<User> users = new ArrayList<>();
        users.add(createUser("admin@example.com", passwordEncoder.encode("admin123"), "Admin", "User",
                "+90 555 100 0001", "Yönetim", "İstanbul", "34000", true, true));
        users.add(createUser("ayse.yilmaz@demo.kapidamart.local", pw, "Ayşe", "Yılmaz",
                "+90 555 200 0001", "Bahçelievler", "İstanbul", "34180", true, true));
        users.add(createUser("mehmet.kaya@demo.kapidamart.local", pw, "Mehmet", "Kaya",
                "+90 555 200 0002", "Çankaya", "Ankara", "06420", true, true));
        users.add(createUser("zeynep.demir@demo.kapidamart.local", pw, "Zeynep", "Demir",
                "+90 555 200 0003", "Karşıyaka", "İzmir", "35590", true, true));
        users.add(createUser("can.ozturk@demo.kapidamart.local", pw, "Can", "Öztürk",
                "+90 555 200 0004", "Nilüfer", "Bursa", "16140", true, true));
        users.add(createUser("elif.sahin@demo.kapidamart.local", pw, "Elif", "Şahin",
                "+90 555 200 0005", "Muratpaşa", "Antalya", "07200", true, true));
        users.add(createUser("burak.celik@demo.kapidamart.local", pw, "Burak", "Çelik",
                "+90 555 200 0006", "Ümraniye", "İstanbul", "34764", false, true));
        users.add(createUser("selin.aktas@demo.kapidamart.local", pw, "Selin", "Aktaş",
                "+90 555 200 0007", "Bornova", "İzmir", "35040", true, true));
        users.add(createUser("emre.polat@demo.kapidamart.local", pw, "Emre", "Polat",
                "+90 555 200 0008", "Kartal", "İstanbul", "34870", true, true));
        users.add(createUser("deniz.vural@demo.kapidamart.local", pw, "Deniz", "Vural",
                "+90 555 200 0009", "Keçiören", "Ankara", "06280", true, true));
        users.add(createUser("melis.kilic@demo.kapidamart.local", pw, "Melis", "Kılıç",
                "+90 555 200 0010", "Gebze", "Kocaeli", "41400", true, true));
        users.add(createUser("kerem.aydin@demo.kapidamart.local", pw, "Kerem", "Aydın",
                "+90 555 200 0011", "Pendik", "İstanbul", "34890", true, true));
        users.add(createUser("ece.gunes@demo.kapidamart.local", pw, "Ece", "Güneş",
                "+90 555 200 0012", "Odunpazarı", "Eskişehir", "26010", false, true));
        users.add(createUser("onur.koc@demo.kapidamart.local", pw, "Onur", "Koç",
                "+90 555 200 0013", "Sarıyer", "İstanbul", "34450", true, true));
        users.add(createUser("defne.ersoy@demo.kapidamart.local", pw, "Defne", "Ersoy",
                "+90 555 200 0014", "Çayyolu", "Ankara", "06810", true, true));
        users.add(createUser("arda.ozkan@demo.kapidamart.local", pw, "Arda", "Özkan",
                "+90 555 200 0015", "Buca", "İzmir", "35390", true, true));
        users.add(createUser("sude.tunc@demo.kapidamart.local", pw, "Sude", "Tunç",
                "+90 555 200 0016", "Bağcılar", "İstanbul", "34200", true, true));
        users.add(createUser("yigit.akar@demo.kapidamart.local", pw, "Yiğit", "Akar",
                "+90 555 200 0017", "Mamak", "Ankara", "06350", true, true));
        users.add(createUser("ceren.bulut@demo.kapidamart.local", pw, "Ceren", "Bulut",
                "+90 555 200 0018", "Konak", "İzmir", "35260", false, true));
        users.add(createUser("kaan.dogan@demo.kapidamart.local", pw, "Kaan", "Doğan",
                "+90 555 200 0019", "Nilüfer", "Bursa", "16120", true, true));
        users.add(createUser("asli.yilmaz@demo.kapidamart.local", pw, "Aslı", "Yılmaz",
                "+90 555 200 0020", "Trabzon Merkez", "Trabzon", "61030", true, true));
        users.add(createUser("furkan.tekin@demo.kapidamart.local", pw, "Furkan", "Tekin",
                "+90 555 200 0021", "Alsancak", "İzmir", "35220", true, true));
        users.add(createUser("pelin.cetin@demo.kapidamart.local", pw, "Pelin", "Çetin",
                "+90 555 200 0022", "Antalya Merkez", "Antalya", "07040", true, true));
        users.add(createUser("umut.erdem@demo.kapidamart.local", pw, "Umut", "Erdem",
                "+90 555 200 0023", "Kadıköy", "İstanbul", "34710", true, true));
        users.add(createUser("naz.korkmaz@demo.kapidamart.local", pw, "Naz", "Korkmaz",
                "+90 555 200 0024", "Çankaya", "Ankara", "06690", true, true));
        userRepository.saveAll(users);
    }

    private User createUser(String email, String password, String firstName, String lastName,
            String phoneNumber, String address, String city, String postalCode,
            Boolean isEmailVerified, Boolean isActive) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setCity(city);
        user.setPostalCode(postalCode);
        user.setIsEmailVerified(isEmailVerified);
        user.setIsActive(isActive);
        return user;
    }

    private void seedReviews() {
        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();
        if (users.isEmpty() || products.isEmpty()) {
            return;
        }

        List<Review> buffer = new ArrayList<>();
        for (Product product : products) {
            List<User> shuffled = new ArrayList<>(users);
            Collections.shuffle(shuffled, random);
            int maxReviews = Math.min(5 + random.nextInt(5), shuffled.size());
            int added = 0;
            for (User user : shuffled) {
                if (added >= maxReviews) {
                    break;
                }
                int rating = pickWeightedRating();
                Review review = new Review();
                review.setRating(rating);
                review.setTitle(REVIEW_TITLES_TR[random.nextInt(REVIEW_TITLES_TR.length)]);
                review.setComment(commentForRating(rating));
                review.setUserId(user.getId());
                review.setProductId(product.getId());
                review.setIsVerified(random.nextDouble() < 0.72);
                review.setIsHelpful(random.nextDouble() < 0.35);
                review.setIsActive(true);
                buffer.add(review);
                added++;
            }
            if (buffer.size() >= 150) {
                reviewRepository.saveAll(buffer);
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            reviewRepository.saveAll(buffer);
        }
    }

    private int pickWeightedRating() {
        double r = random.nextDouble();
        if (r < 0.48) {
            return 5;
        }
        if (r < 0.78) {
            return 4;
        }
        if (r < 0.90) {
            return 3;
        }
        if (r < 0.96) {
            return 2;
        }
        return 1;
    }

    private String commentForRating(int rating) {
        if (rating >= 4) {
            return REVIEW_COMMENTS_POS[random.nextInt(REVIEW_COMMENTS_POS.length)];
        }
        if (rating == 3) {
            return REVIEW_COMMENTS_MID[random.nextInt(REVIEW_COMMENTS_MID.length)];
        }
        return REVIEW_COMMENTS_NEG[random.nextInt(REVIEW_COMMENTS_NEG.length)];
    }
}
