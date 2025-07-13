package software.pxel.accounting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.email.EmailCreateDto;
import software.pxel.accounting.dto.email.EmailUpdateDto;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

@RestController
@RequestMapping("/api/email/{userId}")
@Tag(name = "EmailController", description = "Email data managing")
public class EmailController extends AbstractDataController<EmailData, EmailCreateDto, EmailUpdateDto> {

    public EmailController(
            JwtTokenProvider jwtTokenProvider,
            DataService<EmailData, EmailCreateDto, EmailUpdateDto> service
    ) {
        super(jwtTokenProvider, service);
    }
}