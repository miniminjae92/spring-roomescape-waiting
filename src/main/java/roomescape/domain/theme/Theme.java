package roomescape.domain.theme;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String content;
    private String url;
    @ColumnDefault("30000")
    private long price;

    private Theme(Long id, String name, String content, String url, long price) {
        validate(name, content, url, price);
        this.id = id;
        this.name = name;
        this.content = content;
        this.url = url;
        this.price = price;
    }

    private Theme(String name, String content, String url, long price) {
        this(null, name, content, url, price);
    }

    public static Theme of(Long id, String name, String content, String url) {
        return of(id, name, content, url, 30_000L);
    }

    public static Theme of(Long id, String name, String content, String url, long price) {
        return new Theme(id, name, content, url, price);
    }

    public static Theme createWithoutId(String name, String content, String url) {
        return createWithoutId(name, content, url, 30_000L);
    }

    public static Theme createWithoutId(String name, String content, String url, long price) {
        return new Theme(name, content, url, price);
    }

    private static void validate(String name, String content, String url, long price) {
        if (name == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_NAME);
        }
        if (content == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_CONTENT);
        }
        if (url == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_URL);
        }
        if (price <= 0) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_PRICE);
        }
    }
}
