package software.pxel.accounting.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.pxel.accounting.dto.phone.PhoneUpdateDto;
import software.pxel.accounting.entity.PhoneData;
import software.pxel.accounting.service.DataService;
import software.pxel.accounting.util.JwtTokenProvider;

@RestController
@RequestMapping("/phone")
public class PhoneController extends AbstractDataController<PhoneData, PhoneUpdateDto> {

    public PhoneController(
            JwtTokenProvider jwtTokenProvider,
            DataService<PhoneData, PhoneUpdateDto> service
    ) {
        super(jwtTokenProvider, service);
    }
}