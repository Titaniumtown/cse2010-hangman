import java.util.ArrayList;

public class CharLocMap {
  private int[][] back;
  private int[] endCache;
  private int len;

  public CharLocMap(final int len) {
    final int charNum = 128;
    this.back = new int[charNum][len];
    this.endCache = new int[charNum];
    this.len = len;
  }

  public ArrayList<Integer> keySet() {
    ArrayList<Integer> output = new ArrayList<>();
    for (int i = 0; this.back.length > i; i++) {
      final int[] e = this.back[0];
      if (!e.equals(new int[this.len])) {
        output.add(i);
      }
    }
    return output;
  }

  public void add(final char c, final int pos) {
    final int i = endCache[(int) c];
    this.back[(int) c][i] = pos;

    endCache[(int) c]++;
  }

  public int[] get(final char c) {
    int[] output = new int[this.endCache[(int) c]];
    for (int i = 0; output.length > i; i++) {
      output[i] = this.back[(int) c][i];
    }

    return output;
  }

  public String printPoses(final char c) {
    final int[] result = this.get(c);
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; result.length > i; i++) {
      sb.append(result[i]);
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length() - 1);
    sb.append("]");
    return sb.toString();
  }
}
