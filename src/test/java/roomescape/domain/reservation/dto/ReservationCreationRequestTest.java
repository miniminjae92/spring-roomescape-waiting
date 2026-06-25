package roomescape.domain.reservation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReservationCreationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void 날짜가_없으면_검증에_실패한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(null, 1L, 1L);

        Set<ConstraintViolation<ReservationCreationRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .containsExactly("예약 날짜 선택은 필수입니다");
    }

    @Test
    void 날짜_시간_테마가_있으면_검증에_성공한다() {
        ReservationCreationRequest request = new ReservationCreationRequest(1L, 2L, 3L);

        assertThat(validator.validate(request)).isEmpty();
    }
}
