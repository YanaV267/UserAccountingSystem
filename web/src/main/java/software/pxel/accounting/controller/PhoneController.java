package software.pxel.accounting.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.phone.PhoneCreateDto;
import software.pxel.accounting.dto.phone.PhoneUpdateDto;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

@RestController
@RequestMapping("/api/phone/{userId}")
@Tag(name = "PhoneController", description = "Phone data managing")
public class PhoneController extends AbstractDataController<PhoneData, PhoneCreateDto, PhoneUpdateDto> {

    public PhoneController(
            JwtTokenProvider jwtTokenProvider,
            DataService<PhoneData, PhoneCreateDto, PhoneUpdateDto> service
    ) {
        super(jwtTokenProvider, service);
    }
}