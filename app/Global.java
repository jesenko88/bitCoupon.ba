import helpers.HashHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Category;
import models.Company;
import models.Coupon;
import models.Coupon.Status;
import models.EmailVerification;
import models.FAQ;
import models.Post;
import models.Subscriber;
import models.TransactionCP;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.Play;

public class Global extends GlobalSettings {
	String nameCoupon1 = "Uživajte u dvije noći u Hotelu \"Sunce\" u Neumu za dvije osobe uz doručak!";
	String remarkCoupon1 = "Jedna osoba može kupiti maksimalno četiri kupona za ovu ponudu. Kuponi se mogu spajati.";
	String adress = "Zmaja od Bosne 33.";
	String city = "Sarajevo";
	String contact = "033/333-333";
	String descriptionCoupon1 = "Poželjeli ste da udahnete miris mora i da na bar dva dana pobjegnete od svakodnevnice, da se opustite uz duge šetnje plažom? Uživajte u dvije noći u Hotelu \"Sunce\" u Neumu za dvije osobe uz doručak!";

	String nameCoupon2 = "Mesec dana korištenja teretane + kardio program ! Posljednje pripreme pred ljeto!";
	String remarkCoupon2 = "Ponudom se podrazumeva: Mjesec dana korištenja teretane + kardio program. Ponudom se podrazumijeva 12 termina. Ponuda je SAMO za NOVE članove. Možete kupiti najviše 1 kupon za sebe i više za druge – nove članove.";
	String descriptionCoupon2 = "Još uvek nije kasno da se pokrenete i da svoje zdravlje shvatite kao najprimarniju stvar o kojoj treba da vodite računa.";

	String nameCoupon3 = "Ručak ili večera za dvoje u prekrasnom restoranu hotela Brass ";

	String remarkCoupon3 = "Jedna osoba može kupiti neograničen broj kupona za ovu ponudu. Kupon se može iskoristiti odmah nakon obavljene kupovine. Ponuda se odnosi na večeru za dvoje. Ponuda uključuje jelo po izboru i piće za dvoje.";
	String descriptionCoupon3 = "Poželjeli ste odvesti dragu osobu na romantičnu večeru ili kasni ručak? "
			+ "Hotel Brass unikatan po mnogo čemu: dizajnu, usluzi te uslužnom osoblju."
			+ "Upravo ovakav ambijent začinit će i uljepšati Vašu romantičnu večeru.";


	String nameCoupon4 = "Nezaboravno putovanje u najljepše gradove Mediterana!";
	String remarkCoupon4 = "Platite samo 355 KM za šestodnevno putovanje u italijansku Ligurijsku rivijeru i francusku Azurnu obalu";
	String adress4 = "Grbavicka 33";
	String city4 = "Sarajevo";
	String contact4 = "033/444-444";
	String descriptionCoupon4 = "Ovo putovanje objedinjuje italijansku Ligurijsku rivijeru i francusku Azurnu obalu, fantastične predjele i nevjerovatno lijepe gradove... Pružamo Vam priliku da uživate u mirisima lovora, maslina, mora, jasmina i parfema u vazduhu...";


	// String pic = "images/home/avatar.jpg";

	String nameCoupon5 = "Uživajte u vrhunskim delicijama Pivnice Sarajevo uz ručak ili večeru za dvije osobe!";
	String remarkCoupon5 = "Super cijena za ručak ili večeru za dvije osobe + dva pića po izboru u Pivnici Sarajevo";
	String descriptionCoupon5 = "Ove sedmice iznenadite voljenu osobu, prijatelja ili prijateljicu, ručkom ili večerom u Pivnici Sarajevo. Odmorite se od svakodnevnog kuhanja i pripremanja istih jela i priuštite sebi i dragoj osobi uživanje u ukusnim delicijama i predivnom ambijentu Pivnice!";

	String nameCoupon6 = "Rafting avantura na Neretvi uz licencirane skipere i čak 57% popusta!";
	String remarkCoupon6 = "Platite 34 KM umjesto 80 KM za potpuno organiziranu turu raftinga na jednoj od najljepših rijeka svijeta";
	String descriptionCoupon6 = "Rafting je uzbudljiva pustolovina za one mirne, a pogotovo za one željne dobre doze adrenalina. Na raftingu ćete bolje upoznati svoje kolege ili prijatelje, u drugačijem okruženju i neobičnim situacijama, a upoznat ćete i prirodne ljepote Neretve za koje još do sada možda i niste znali.";

	String nameCoupon7 = "Proslavite praznik rada u Beču i odmorite se po Super cijeni!";
	String remarkCoupon7 = "Platite samo 219 KM organizovano putovanje u Beč za Prvi maj!";
	String descriptionCoupon7 = "Beč je vijekovima bio prijestolnica Habsburške monarhije, a sada je glavni grad bogate Austrije. Beč je grad slavnih koncertnih dvorana i vrhunskih koncerata, bogatih muzeja, opere i pozorišta, što ga uz neizostavnu kupovinu u Mariahlifer strasse, čini veoma primamljivim za turiste. ";

	String nameCoupon8 = "Trening plivanja i/ili vaterpola za Vaše mališane u PVK Dabar!!";
	String remarkCoupon8 = "Mjesečnu članarinu za trening plivanja i/ili vaterpola (za djecu 8-15 godina) platite samo 25 KM umjesto 50 KM";
	String descriptionCoupon8 = "Plivanje se smatra jednim od najzdravijih sportova jer jača mišiće cijelog tijela, podiže vitalnost i jača kardiovaskularni sistem. Za djecu je plivanje posebno bitno jer potiče razvoj djeteta, jača pluća i srce, poboljšava imunitet te razvija osjećaj koordinacije pokreta i samokontrole.";

	String nameCoupon9 = "Odmorite se od duge zime i uživajte u toplim danima na Jadranu!";
	String remarkCoupon9 = "Platite samo 239 KM dva noćenja na bazi polupansiona za dvije osobe u hotelu Plaža, Drašnice";
	String descriptionCoupon9 = "Sunčano vrijeme i idealna temperatura zraka i mora, razlog su zbog kojeg mnogi svoj godišnji odmor planiraju i prije ljeta. Ako ne volite ljetnu gužvu i potreban Vam je pravi odmor od duge zime, preporučujemo Vam da iskoristite ovu fantastičnu ponudu i s voljenom osobom odete na sedmodnevni proljetni odmor u hotel Plaža u Drašnicama.";

	String post_content = " <p>Never in all their history have men been able truly to conceive of the world as one: a single sphere, a globe, having the qualities of a globe, a round earth in which all the directions eventually meet, in which there is no center because every point, or none, is center — an equal earth which all men occupy as equals. The airman's earth, if free men make it, will be truly round: a globe in practice, not in theory.</p>"
			+ "  <p>Science cuts two ways, of course; its products can be used for both good and evil. But there's no turning back from science. The early warnings about technological dangers also come from science.</p>"
			+ "  <p>What was most significant about the lunar voyage was not that man set foot on the Moon but that they set eye on the earth.</p>"
			+ "   <p>A Chinese tale tells of some men sent to harm a young girl who, upon seeing her beauty, become her protectors rather than her violators. That's how I felt seeing the Earth for the first time. I could not help but love and cherish her.</p>"
			+ "   <p>For those who have seen the Earth from space, and for the hundreds and perhaps thousands more who will, the experience most certainly changes your perspective. The things that we share in our world are far more valuable than those which divide us.</p>"
			+ " <h2 class=\"section-heading\">The Final Frontier</h2>"
			+ "<p>There can be no thought of finishing for ‘aiming for the stars.’ Both figuratively and literally, it is a task to occupy the generations. And no matter how much progress one makes, there is always the thrill of just beginning.</p>"
			+ "<p>There can be no thought of finishing for ‘aiming for the stars.’ Both figuratively and literally, it is a task to occupy the generations. And no matter how much progress one makes, there is always the thrill of just beginning.</p>"
			+ "<blockquote>The dreams of yesterday are the hopes of today and the reality of tomorrow. Science has not yet mastered prophecy. We predict too much for the next year and yet far too little for the next ten.</blockquote>"
			+ "<p>Spaceflights cannot be stopped. This is not the work of any one man or even a group of men. It is a historical process which mankind is carrying out in accordance with the natural laws of human development.</p>"
			+ " <h2 class=\"section-heading\">Reaching for the Stars</h2>"
			+ " <p>As we got further and further away, it [the Earth] diminished in size. Finally it shrank to the size of a marble, the most beautiful you can imagine. That beautiful, warm, living object looked so fragile, so delicate, that if you touched it with a finger it would crumble and fall apart. Seeing this has to change a man.</p>"
			+ "<a href=\"#\">"
			+ " <img class= \"img-responsive\" src=\"img/post-sample-image.jpg\" alt=\"\">"
			+ " </a>"
			+ " <span class=\"caption text-muted\">To go places and do things that have never been done before – that’s what living is all about.</span>"
			+ " <p>Space, the final frontier. These are the voyages of the Starship Enterprise. Its five-year mission: to explore strange new worlds, to seek out new life and new civilizations, to boldly go where no man has gone before.</p>"
			+ " <p>As I stand out here in the wonders of the unknown at Hadley, I sort of realize there’s a fundamental truth to our nature, Man must explore, and this is exploration at its greatest.</p>"
			+ " <p>Placeholder text by</p>";

	@Override
	public void onStart(Application app) {

		Category food = null;
		Category travel = null;
		Category sport = null;
		Category other = null;
		Company admin = null;
		Company bitCamp = null;
		long ownedCoupinID1 = 0;
		long ownedCoupinID2 = 0;
		int quantity = 1;
		int status = Coupon.Status.ACTIVE;
		String picture = Play.application().configuration()
				.getString("defaultProfilePicture");

		if (!Company.exists("Admin")) {
			bitCamp = new Company("Admin", "bitcouponadmin@gmail.com",
					HashHelper.createPassword("bitadmin"), new Date(), picture,
					adress, city, contact);
			bitCamp.status = Status.ACTIVE;
			bitCamp.save();
		}

		if (!Company.exists("BitCamp")) {
			bitCamp = new Company("BitCamp", "bitcamp@bitcamp.ba",
					HashHelper.createPassword("bitcamp"), new Date(), picture,
					adress, city, contact);
			bitCamp.status = Status.ACTIVE;
			bitCamp.save();
		}

		if (Category.exists("Food") == false) {
			food = new Category("Food", "http://res.cloudinary.com/bitfiles/image/upload/v1430902947/staticImages/food.png");
			food.save();
		}
		if (Category.exists("Travel") == false) {
			travel = new Category("Travel", "http://res.cloudinary.com/bitfiles/image/upload/v1430902953/staticImages/travel.png");
			travel.save();
		}
		if (Category.exists("Sport") == false) {
			sport = new Category("Sport", "http://res.cloudinary.com/bitfiles/image/upload/v1430902950/staticImages/sport.png");
			sport.save();
		}

		if(Category.exists("Other") == false){
			other = new Category("Other");
			other.save();

		}

		if (Coupon.checkByName(nameCoupon1) == false) {
			Coupon.createCoupon(nameCoupon1, 80, new Date(), "http://res.cloudinary.com/bitfiles/image/upload/v1430853980/staticImages/cp1.jpg",
					travel, descriptionCoupon1, remarkCoupon1, 5, 25,
					new Date(), bitCamp, status);
		}
		if (Coupon.checkByName(nameCoupon2) == false) {

			ownedCoupinID1 = Coupon.createCoupon(
					nameCoupon2,
					40,
					new Date(),
					"http://res.cloudinary.com/bitfiles/image/upload/v1430853989/staticImages/cp2.jpg",
					other , descriptionCoupon2,
					remarkCoupon2, 5, 20 ,new Date(), bitCamp, status);

		}
		/* creating a coupon that is not expired */
		if (Coupon.checkByName(nameCoupon3) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "01/01/2051";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			ownedCoupinID2 = Coupon.createCoupon(nameCoupon3, 20, date,
					"http://res.cloudinary.com/bitfiles/image/upload/v1430854001/staticImages/cp3.jpg", food,
					descriptionCoupon3, remarkCoupon3, 2, 5, new Date(),
					bitCamp, status);
		}

		if (Coupon.checkByName(nameCoupon4) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Coupon.createCoupon(nameCoupon4, 350, date, "http://res.cloudinary.com/bitfiles/image/upload/v1430854005/staticImages/cp4.jpg",
					travel, descriptionCoupon4, remarkCoupon4, 5, 30,
					new Date(), bitCamp, status);
		}

		if (Coupon.checkByName(nameCoupon5) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Coupon.createCoupon(
					nameCoupon5,
					17,
					date,
					"http://res.cloudinary.com/bitfiles/image/upload/v1430854008/staticImages/cp5.jpg",
					food, descriptionCoupon5,
					remarkCoupon5, 5, 30 ,new Date(),bitCamp, status);

		}

		if (Coupon.checkByName(nameCoupon6) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Coupon.createCoupon(nameCoupon6, 34, date, "http://res.cloudinary.com/bitfiles/image/upload/v1430854011/staticImages/cp6.jpg", travel, descriptionCoupon6, remarkCoupon6, 5, 30,
					new Date(), bitCamp, status);
		}

		if (Coupon.checkByName(nameCoupon7) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Coupon.createCoupon(nameCoupon7, 219, date, "http://res.cloudinary.com/bitfiles/image/upload/v1430854013/staticImages/cp7.jpg", travel, descriptionCoupon7, remarkCoupon7, 5, 30,
					new Date(), bitCamp, status);
		}

		if (Coupon.checkByName(nameCoupon8) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Coupon.createCoupon(nameCoupon8, 25, date, "http://res.cloudinary.com/bitfiles/image/upload/v1430854016/staticImages/cp8.jpg", sport, descriptionCoupon8, remarkCoupon8, 5, 30,
					new Date(), bitCamp, status);
		}

		if (Coupon.checkByName(nameCoupon9) == false) {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String dateString = "05/05/2020";
			Date date = null;
			try {
				date = df.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Coupon.createCoupon(nameCoupon9, 239, date, "http://res.cloudinary.com/bitfiles/image/upload/v1430854019/staticImages/cp9.jpg", travel, descriptionCoupon9, remarkCoupon9, 5, 30,
					new Date(), bitCamp, status);
		}

		if (User.check("bitcoupon@gmail.com") == false) {
			User.createUser("Admin", "", new Date(), "", "", "",
					"bitcoupon@gmail.com",
					HashHelper.createPassword("bitadmin"), true, picture);
			EmailVerification setVerified = new EmailVerification(1, true);
			setVerified.save();
		}

		if (User.check("jesenko.gavric@bitcamp.ba") == false) {
			User user = new User("Jesenko", "Gavric", new Date(), "", "", "",
					"jesenko.gavric@bitcamp.ba",
					HashHelper.createPassword("johndoe"), false, picture);
			
			user.save();
			EmailVerification setVerified = new EmailVerification(2, true);
			setVerified.save();
			Coupon c1 = Coupon.find(ownedCoupinID1);
			Coupon c2 = Coupon.find(ownedCoupinID2);
			TransactionCP.createTransaction("AH-324ASDas", "bitSale",  c1.price, quantity,
					c1.price, "TOKEN01010", user, c1);
			c1.maxOrder = c1.maxOrder - quantity;
			c1.statistic.bought(quantity);
			c1.save();
			TransactionCP.createTransaction("AH-324ASsadD", "bitSale", c2.price, quantity,
					c2.price, "TOKEN2222", user, c2);
			c2.maxOrder = c2.maxOrder - quantity;
			c2.statistic.bought(quantity);
			c2.save();

		}

		if (User.check("vedad.zornic@bitcamp.ba") == false) {
			User.createUser("Vedad", "Zornic", new Date(), "", "", "",
					"vedad.zornic@bitcamp.ba",
					HashHelper.createPassword("johndoe"), false, picture);
			EmailVerification setVerified = new EmailVerification(3, true);
			setVerified.save();
			Subscriber sb = new Subscriber(
					User.findByEmail("vedad.zornic@bitcamp.ba"));
			sb.save();
		}

		if (FAQ.checkByQuestion("Želim kupiti današnju ponudu. Kako da to učinim?") == false) {
			FAQ.createFAQ(
					"Želim kupiti današnju ponudu. Kako da to učinim?",
					"Morate biti "
							+ "registrovani i ulogovani na stranicu. Ukoliko još nemate nalog,"
							+ " registracija je besplatna i traje manje od 30 sekundi, a također se "
							+ "možete uvezati sa Vašim Facebook nalogom. Kada se ulogujete kliknite na"
							+ " dugme Kupi i pratite jednostavne korake.");
		}

		if (FAQ.checkByQuestion("Na koji način plaćam ponudu i je li sigurno?") == false) {
			FAQ.createFAQ(
					"Na koji način plaćam ponudu i je li sigurno?",
					"Plaćanje možete obaviti na"
							+ " jedan od sljedećih načina:"
							+ "1.	Putem eKredita – eKredit možete uplatiti u banci ili pošti na naš transkcijski "
							+ "račun, a on će Vam biti dodijeljen sljedeći radni dan ili najkasnije 48 sati nakon"
							+ " uplate. Kada Vaš eKredit bude prikazan na Vašem profilu možete obaviti željenu kupovinu. "
							+ "Više informacija možete pronaći na ovom linku nakon prijave na Vaš profil."
							+ "2.	Putem kartice – Plaćanje je zbog sigurnosti, jednostavnije realizacije i brzine "
							+ "obrade zahtjeva limitirano na kreditne i debitne MasterCard, Maestro i VISA kartice. "
							+ "Plaćanje je potpuno sigurno, a više o tome možete pročitati na - sigurnost plaćanja."
							+ "3.	Dio plaćate eKreditom, a dio karticom – Prilikom kupovine potrebno je da unesete iznos "
							+ "eKredita koji želite da Vam se odbije i dalje idete na „Nastavi kupovinu“. Unesete sve"
							+ " potrebne podatke i na taj način će Vam se jedan dio odbiti u vidu eKredita a ostatak"
							+ " vrijednosti će biti rezervisan na Vašoj kartici."
							+ "4. Gotovinska uplata kupona ili ekredita - uplatu možete izvršiti na eKupon prodajnim mjestima u"
							+ " BBI tržnom centru i Grand centru na Ilidži. "
							+ "5. Kupovina poklon bona na ime i prezime osobe koju želite bez obzira da li je registrovan korisnik ili ne.");
		}

		if (FAQ.checkByQuestion("Šta se dešava ako ponuda dana ne dosegne minimalan broj kupaca?") == false) {
			FAQ.createFAQ(
					"Šta se dešava ako ponuda dana ne dosegne minimalan broj kupaca?",
					"Da bi ponuda "
							+ "uspjela mora dostići minimalan broj kupaca koji se određuje u dogovoru s partnerima."
							+ "U slučaju da se ne kupi dovoljan broj kupona, ponuda propada i ništa Vam se ne naplaćuje. "
							+ "Da izbjegnete ovakve situacije, pozovite što više osoba da se uključe na ponudu na "
							+ "kojoj ste učestvovali.");
		}
		//Creating 10 testing posts.
		if(Post.all().size() < 1){
			for(int i=0; i<10; i++){
				Post.createPost("Man must explore, and this is exploration at its greatest",
						"Problems look mighty small from 150 miles up"
						, post_content, "http://res.cloudinary.com/bitfiles/image/upload/v1430859169/staticImages/explore.jpg", new Date(), User.find(true), "no,tag");				
			}
		}
	}
}
