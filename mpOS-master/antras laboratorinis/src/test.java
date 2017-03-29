import Rm.Rm;

/**
 * Created by blitZ on 3/8/2017.
 */
public class test {
    Rm temp;
    public test(Rm rm) {
        temp = rm;
    }

    public void testSf(){
        temp.sf.setCf(1);
        temp.sf.print();
        temp.sf.setOf(1);
        temp.sf.setCf(0);
        temp.sf.print();
        temp.sf.setZf(1);
        temp.sf.setCf(1);
        temp.sf.print();
        temp.sf.setZf(0);
        temp.sf.setCf(0);
        temp.sf.setOf(0);

    }
}
