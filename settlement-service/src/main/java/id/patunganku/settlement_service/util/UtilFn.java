package id.patunganku.settlement_service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class UtilFn {

    private UtilFn() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getCurrentPath() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return request.getRequestURI();
        }

        return null;
    }
}
