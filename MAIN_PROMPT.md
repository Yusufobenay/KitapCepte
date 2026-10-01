# Kitap Cepte

Selamlar ben bir Android uygulaması yapmak istiyorum, Bu uygulama bir e ticaret uygulaması ve bu uygulamada kitap satışı yapılacak. 

Uygulama içeriği ;
——————————

1- Onboarding Ekranları;

3 Ekranlı bir onboarding süreci olacak ilk iki ekranda uygulama tanıtımı yapılsın, son ekran da ise uygulamada nasıl ilerleriz konusunu anlatsın login register alanına yönlendirme buradan login register alanına yönlendirme yapılacak. 


2- Login Register Ekranları;

Login ekranı için; Maile giriş yap , ya da misafir giriş yap bu iki seçenek olsun buradan eğer user mail ile giriş yap dediği durumda hesabı yoksa register kısmına yönlendirilsin ve burada kayıt aşamasına geçilsin 

3- Home Ekranı ; 

Top barın sağ üstünde profil iconu olacak buradan profil ekranına yönlendirme yapılabilecek ,tam ortada Kitap Cepte yazmasını istiyorum,

Bu top altında bir chip listesi olsun bu liste ile selected, un selected olarak chipler ile filtreleme yapılacak,

Bu chip altında bir kitap listesi olacak bu kitap listesi olacak ama grid bir şekilde olsun , her grid içinde 2 adet olsun 

Ürün cardları için ; Ürün görseli ,sağ üst köşede favori butonu , ürün görselinin altında ürün ismi ve bir buton , buton texti ürün fiyatı olacak, Bu card tasarımını referans olarak ekledim card1 buradan inceleyebilirsin.

En altta bir bottom bar var bu bottom bar içerisinde ; 

1-Home Ekranı ,2- Favori Ekranı , 3-Profil Ekranı Bu 3 ekran bottom bar da olacak 

4- Detay Ekranı 

Burada bir top bar olacak ; Sol köşede geri butonu ortada ekran ismi sağ da ise favori alma veya çıkarma, Buranın altında ürün görseli olacak,onun altında ismi ,açıklaması varsa benzer kitaplar ve ürün fiyatı yazacak burdan charta eklenebilecek. Bu ekranın referans görselini iletiyorum buradan analiz edip ilerleyebilirsin

5- Chart Ekranı 

Burada bir top bar olacak title olarak ekran ismi ve sağ üst köşede profil iconu, Chart kısmında bir horizontal card listemiz var burada satın alınmak istenen ürün listeleri olacak 

Ürün cardları ; Ürün görseli , ismi ,fiyatı sepette ki ürün sayısı ve silme icon, referans olarak card2 görselini analiz edebilirsin en alt kısımda sepette ki ürünlerin toplam fiyatı vergi ve ödemeler olarak özet alanı olsun onun altında da total fiyatın yazdığı ve tıklandıgın da ödeme ekranına yönlendiren bir buton olsun 


6- Ödeme Ekranı

Kullanıcı card bilgilerini girebilecek buradan ödeme yapılacak card bilgileri girilip ödeme yapıldıktan sonra başarılı mesajı gösterip home ekranına geri yollanacak 

7- Profil Ekranı

Burada kullanıcı bilgileri , şifreler, cardlar vs alanlarının olduğu kısımlar olacak 


*** Misafir olarak giriş yapan kullanıcı Home ekranında ürün detay ekranı hariç ayrıca bottom bar da ki favori ve profil kısımları hariç alanları kullanabilecek, eğer diğer alanları kullanmak isterse burada bir bottom sheet çıkarıp üye olmasını sağlayacağız ***


Rulles:

- Her faz bittiğinde bir önce ki faza geçmeden projeyi bir derleyeceksin daha sonra onayıma sunup onay aldıktan sonra diğer faza geçeceksin
- Her onay sonrası o faz da yapılanları commit. Push atacaksın 


Burada Home ekranı detay ve char ekranı tasarımları ilettiğim referans “main” görselinde ki gibi olacak buna göre tasarımı ilerletebilirsin. 


—————————

Renkler, Butonlar Cardlar … ,

Ana renkler
* Ana mor/indigo (butonlar, seçili sekme, toplam fiyat butonu): #5B3FE8
* Açık mor (ikincil butonlar, "In cart" arka planı, seçili kart vurgusu): #E8E3FB
* Arka plan gradyanı: sol üst #6E7BE6 (mavimsi) ile sağ alt #A08BEF (lavanta) arasında
Yüzeyler
* Telefon ekranı / kart zemini: #EEEBF8 (hafif mor tonlu beyaz)
* Saf beyaz (kartlar, butonlar): #FFFFFF
Metin
* Koyu başlık/metin: #1F1B33
* Gri ikincil metin (açıklamalar, eski fiyat): #8D8AA3
Vurgu renkleri
* "Top Item" rozeti sarısı: #FFD93B
* Sepet bildirim rozeti kırmızısı: #F04A3C



** Kullanınlan componentler ortak bir yapıda olmalı aynı compenentler her yerde kullanılacağı için kod tekrarı olmasın 
Titler fontlar textler renkler gibi alanları tek bir yerden değiştirebilir olmak istiyorum


——————

 Proje ;

Android uygulaması olacak Kotlin ile yazılacak , moduler yapı olmasın ama mvvm uygun olsun , son teknoloji kullanılsın , clean olsun test edilebilir olsun (Test yazma)


Backend ;

https://openlibrary.org/search.json?q=subject:fantasy&limit=30 

Bu api kullanılacak ; 
Bu api response modeli ; 
```json
{
  "numFound": 119275,
  "start": 0,
  "numFoundExact": true,
  "num_found": 119275,
  "documentation_url": "https://openlibrary.org/dev/docs/api/search",
  "q": "subject:fantasy",
  "offset": null,
  "docs": [
    {
      "author_key": [
        "OL2801083A"
      ],
      "author_name": [
        "Joe Abercrombie"
      ],
      "cover_edition_key": "OL7878939M",
      "cover_height": 824,
      "cover_i": 14543422,
      "cover_width": 586,
      "ebook_access": "printdisabled",
      "edition_count": 16,
      "first_publish_year": 2001,
      "has_fulltext": true,
      "ia": [
        "bladeitself0000aber",
        "bladeitself00aber",
        "bladeitself0000aber_d4a5",
        "bladeitself0000aber_m3v2"
      ],
      "ia_collection": [
        "americanuniversity-ol",
        "denverpubliclibrary-ol",
        "internetarchivebooks",
        "openlibrary-d-ol",
        "printdisabled",
        "wilsoncollege-ol",
        "worthingtonlibraries-ol"
      ],
      "key": "/works/OL8400950W",
      "language": [
        "eng"
      ],
      "public_scan_b": false,
      "title": "The Blade Itself"
    }
  ]
}
```

Bu modeli kullanıp gerekli home ekranı gibi ekranlarda kullanmak istiyoruz. Bu api bir GET buna göre eklemesini yapmalısın 


Şimdi senden istediğim bu bilgileri kullanarak bana bir ui skill , mimari skill , be skill ,teknolijiler için tech_skill , projeyi ekran bazlı fazlandırmanı istiyorum her ekran bir faz olacak buna göre de ilerleyeceğiz.
Bir de bana rules_skill , Bir de master promt olsun bu promtta proje içeriği kullanılacaklar vs olacak ayrıca projeyi başlatmadan önce bir implementation planı oluştursun bana bu istediklerimi yapar mısın.  bunu da bir md dosyasına ekleyip main promt diye ekleyip commit push atar mısın 
