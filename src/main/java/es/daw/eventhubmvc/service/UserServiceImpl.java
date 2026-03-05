package es.daw.eventhubmvc.service;

import es.daw.eventhubmvc.dto.UserProfileUpdateRequest;
import es.daw.eventhubmvc.entity.User;
import es.daw.eventhubmvc.exception.EmailAlreadyExistsException;
import es.daw.eventhubmvc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
//@Transactional // en observación????
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Username not found")
                );
    }

    @Override
    public User updateProfile(Long userId, UserProfileUpdateRequest request) {

        // Cargo el usuario de BD con el id
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Username not found")
                );

        // Email único excepto el propio usuario
        // select * from users where email = request.email() and id != userId
        if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Actualizar el usuario con los nuevos datos
        user.setFullName(request.fullname());
        user.setEmail(request.email());

        // Salvar en la BD el usuario actualizado
        return userRepository.save(user);
    }
}
