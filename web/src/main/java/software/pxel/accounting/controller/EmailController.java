package software.pxel.accounting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.email.EmailUpdateDto;
import software.pxel.accounting.entity.EmailData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

@RestController
@RequestMapping("email")
@Tag(name = "EmailController", description = "Email data of users")
public class EmailController extends AbstractDataController<EmailData, EmailUpdateDto> {

    public EmailController(
            JwtTokenProvider jwtTokenProvider,
            DataService<EmailData, EmailUpdateDto> service
    ) {
        super(jwtTokenProvider, service);
    }
}