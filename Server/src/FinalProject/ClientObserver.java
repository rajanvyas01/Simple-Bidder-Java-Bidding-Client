/*
 *  EE422C Final Project submission by
 *  Replace <...> with your actual data.
 *  <Rajan Vyas>
 *  <rv23454>
 *  <16160>
 *  Fall 2020
 */

package FinalProject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends ObjectOutputStream implements Observer {

    public ClientObserver(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.reset();
            this.writeObject(arg);
            this.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
