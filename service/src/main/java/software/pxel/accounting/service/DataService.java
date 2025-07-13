package software.pxel.accounting.service;

import lombok.RequiredArgsConstructor;
import software.pxel.accounting.dto.AbstractDataCreateDto;
import software.pxel.accounting.dto.AbstractDataUpdateDto;
import software.pxel.accounting.entity.AbstractData;
import software.pxel.accounting.repository.DataRepository;
import software.pxel.accounting.repository.UserRepository;

@RequiredArgsConstructor
public abstract class DataService<E extends AbstractData, C extends AbstractDataCreateDto,
        U extends AbstractDataUpdateDto> {
    protected final UserRepository userRepository;
    protected final DataRepository<E> dataRepository;

    public abstract void create(Long userId, C dto);

    public abstract void update(Long userId, U dto);

    public abstract void delete(Long userId, String data);
}
