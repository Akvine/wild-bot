package ru.akvine.marketspace.bot.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Request utils tests")
public class RequestUtilsTest {
    @Test
    @DisplayName("Return origin url if query params is null")
    public void return_origin_url_if_query_params_is_null() {
        String url = "Some url";
        Map<String, String> queryParams = null;

        String uri = RequestUtils.buildUri(url, queryParams);

        assertThat(uri).isNotBlank();
        assertThat(uri).isEqualTo(url);
    }

    @Test
    @DisplayName("Return origin url if query params is empty")
    public void return_origin_url_if_query_params_is_empty() {
        String url = "Some url";
        Map<String, String> queryParams = Map.of();

        String uri = RequestUtils.buildUri(url, queryParams);

        assertThat(uri).isNotBlank();
        assertThat(uri).isEqualTo(url);
    }

    @Test
    @DisplayName("Return url with params")
    public void return_url_with_params() {
        String url = "url";
        String expected = "url?param1=value1&param2=value2";

        Map<String, String> queryParams = Map.of(
                "param1", "value1",
                "param2", "value2"
        );

        String result = RequestUtils.buildUri(url, queryParams);

        assertThat(result).isNotBlank();
        assertThat(result).isEqualTo(expected);
    }
}
