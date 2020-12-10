
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class test {

    class Pair{
        int left;
        int right;
        public Pair(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return left == pair.left &&
                    right == pair.right;
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }
    }

    public static void main(String[] args) {
        StringBuilder resString = new StringBuilder("a about\t5722018508:1\t5722018496:1\t5722018101:1\t5722018301:1");
        String id = "5722018301:2";
        if(id.contains(":")) {
            int newVal = Integer.valueOf(id.substring(11).trim());
            id = id.substring(0, 10);
            if (resString.toString().contains(id)) {
                // : = idx
                int startIdx = resString.toString().indexOf(id);
                int idx = resString.toString().indexOf(id) + 11;
                int curVal = 0;
                while(idx < resString.length() && Character.isDigit(resString.toString().charAt(idx))) {
                    curVal = curVal * 10 + ((resString.toString().charAt(idx)) - '0');
                    idx++;
                }
                String newString = id + ":" + (curVal + newVal);
                String newResult = resString.substring(0, startIdx) + newString + (idx == resString.length() ? "\t" : (resString.charAt(idx) == '\t' ? resString.substring(idx) : "\t" + resString.substring(idx)));
                resString = new StringBuilder(newResult);
            } else {
                resString.append(id);
            }
        }
//        System.out.println(resString.toString());
//
//        StringBuilder te = new StringBuilder("avdccc");
//        System.out.println(te.charAt(5));
        String sss = "8888888888:2\t77777:2";
        int idx = sss.indexOf("8888888888");
        System.out.println(sss.substring(idx+11));
        System.out.println(sss.substring(11).trim());
    }
}
