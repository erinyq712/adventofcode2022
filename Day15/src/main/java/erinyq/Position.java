package erinyq;

public record Position(long x, long y) {

    public Position add(Position p) {
        return new Position(x + p.x, y + p.y);
    }

    public Position subtract(Position p) {
        return new Position(x - p.x, y - p.y);
    }

    public Position abs() {
        return new Position(Math.abs(x), Math.abs(y));
    }

    public long length() {
        var a = abs();
        return a.x + a.y;
    }

    public int compare(Position p) {
        int xc = Long.compare(this.x, p.x);
        if (xc == 0) {
            return Long.compare(this.y, p.y);
        } else {
            return xc;
        }
    }
}
