package felica;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.util.List;

public class CardReader implements Runnable {

    private CardReaderCallback cardReaderCallback;

    public CardReader() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void addCardReaderCallback(CardReaderCallback cardReaderCallback) {
        this.cardReaderCallback = cardReaderCallback;
    }

    @Override
    public void run() {

        try {
            // Display the list of terminals
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);

            // Use the first terminal
            CardTerminal terminal = terminals.get(0);

            while(true) {
                try {
                    System.out.println("Please Insert the card!!");
                    terminal.waitForCardPresent(0);

                    Card card = terminal.connect("*");
                    System.out.println("Card: " + card);
                    CardChannel channel = card.getBasicChannel();

                    FelicaManager felicaManager = FelicaManager.getInstance(channel);

                    if (cardReaderCallback != null) {
                        cardReaderCallback.isCardPresent(felicaManager);
                    }
                    System.out.println("Wait before inserting the card!!");
                    Thread.sleep(5000);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}