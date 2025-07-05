package software.pxel.accounting.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import software.pxel.accounting.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
