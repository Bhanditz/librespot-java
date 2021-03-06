package xyz.gianlu.librespot.player.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.gianlu.librespot.common.Utils;
import xyz.gianlu.librespot.common.proto.Spirc;
import xyz.gianlu.librespot.mercury.model.PlayableId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gianlu
 */
public class Remote3Track {
    public final String uri;
    public final String uid;
    public final JsonObject metadata;
    private PlayableId id;

    Remote3Track(@NotNull JsonObject obj) {
        uri = Utils.optString(obj, "uri", null);
        uid = Utils.optString(obj, "uid", null);
        metadata = obj.getAsJsonObject("metadata");
    }

    @Nullable
    public static Remote3Track opt(@NotNull JsonObject obj, @NotNull String key) {
        JsonElement elm = obj.get(key);
        if (elm == null || !elm.isJsonObject()) return null;
        return new Remote3Track(elm.getAsJsonObject());
    }

    @Nullable
    public static List<Remote3Track> optArray(@NotNull JsonObject obj, @NotNull String key) {
        JsonElement elm = obj.get(key);
        if (elm == null || !elm.isJsonArray()) return null;
        JsonArray array = elm.getAsJsonArray();

        List<Remote3Track> list = new ArrayList<>(array.size());
        for (JsonElement track : array)
            list.add(new Remote3Track(track.getAsJsonObject()));

        return list;
    }

    public void addToState(@NotNull Spirc.State.Builder builder) {
        Spirc.TrackRef ref = toTrackRef();
        if (ref != null) builder.addTrack(ref);
    }

    public void addTo(@NotNull List<Spirc.TrackRef> list) {
        Spirc.TrackRef ref = toTrackRef();
        if (ref != null) list.add(ref);
    }

    @Nullable
    public Spirc.TrackRef toTrackRef() {
        if (Objects.equals(uri, "spotify:delimiter"))
            return null;

        boolean isQueued = false;
        if (metadata != null) {
            JsonElement elm = metadata.get("is_queued");
            if (elm != null) isQueued = elm.getAsBoolean();
        }

        return Spirc.TrackRef.newBuilder()
                .setQueued(isQueued)
                .setGid(ByteString.copyFrom(id().getGid()))
                .setUri(uri).build();
    }

    @NotNull
    public PlayableId id() {
        if (id == null) id = PlayableId.fromUri(uri);
        return id;
    }
}
