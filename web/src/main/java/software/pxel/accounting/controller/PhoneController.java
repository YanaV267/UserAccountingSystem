package software.pxel.accounting.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.phone.PhoneUpdateDto;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

@RestController
@RequestMapping("/phone")
@Tag(name = "PhoneController", description = "Phone data of users")
public class PhoneController extends AbstractDataController<PhoneData, PhoneUpdateDto> {

    public PhoneController(
            JwtTokenProvider jwtTokenProvider,
            DataService<PhoneData, PhoneUpdateDto> service
    ) {
        super(jwtTokenProvider, service);
    }
}