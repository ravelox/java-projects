/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.1  99/07/06  12:28:59  12:28:59  dkelly (Dave Kelly)
Initial revision

*/

import java.awt.*;

class NoFocusButton extends Button
{
        @Override
        public void requestFocus() {}
        public NoFocusButton(String s) {
                super(s);
                setFocusable(false);
        }
}
