## Uppgift 3 - Hangman
*I uppgift Hangman (Hänga gubbe), så skall ni skapa spelet Hangman. Använd er av minst en Class, varav en ska vara “private” class med själva ordet som ska gissas*

*Ni behöver minst ha en form av menu där spelaren kan välja följande:*  
*Game status = Visar antalet gissningar och hur många spelaren har kvar.*  
*Guess char= Låter spelaren gissa en bokstav*  
*Guess word=Låter spelaren gissa ordet*

*Ni ska även “rita” ut själva gubben när det passar. Självklart så ska den “ritas” steg för steg under spelets gång.*

*Spelregler och svårighetsgrad sätter ni själva.*

*Bonus 1 är om ni kan visa vilken bokstav som passar var i ordet som spelet frågar efter, ex: med ordet “java” så om spelaren gissar “a” så ska “_a_a” eller liknande visas.*

*Bonus 2 är om ni uttnyttar ett object på något vis*  

## Dokumentation  
Fungerande skick men inte så fina animationer som jag hade tänkt... Orden hämtas från en "random word API", därför behövs internetuppkoppling.

Har försökt att paketera projektet i en jar-fil för att enklare kunna köra det utan att installera JavaFX m.m. men det verkar omöjligt att få till en fil som fungerar att köra i Windows och Linux oberoende av JDK. Vad som verkar fungera är att importera projektet i Eclipse och starta det därifrån, annars får man installera JavaFX och se till att modulerna hittas, t.ex. `--module-path %PATH_TO_FX% --add-modules javafx.controls`

![](demo/hangman.gif)