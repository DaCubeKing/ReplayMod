package eu.crushedpixel.replaymod.holders;

public class Keyframe {

    private int realTimestamp;

    public Keyframe clone() {
        return new Keyframe(realTimestamp);
    }

    public Keyframe(int realTimestamp) {
        this.realTimestamp = realTimestamp;
    }

    public int getRealTimestamp() {
        return realTimestamp;
    }

    public void setRealTimestamp(int realTimestamp) { this.realTimestamp = realTimestamp; }

    @Override
    public boolean equals(Object o2) {
        if(o2 == null) return false;
        if(!(o2 instanceof Keyframe)) return false;
        Keyframe kf = (Keyframe)o2;
        return hashCode() == kf.hashCode();
    }

    @Override
    public int hashCode() {
        return realTimestamp;
    }
}
