package br.com.damasceno.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.damasceno.todolist.users.IUserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {



    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                if (request.getServletPath().startsWith("/tasks/") ) {
                    
                    
                            //get authentication (user and pw)
                            var authorization = request.getHeader("Authorization");
            
                            var authEncoded = authorization.substring("Basic".length()).trim();
            
                            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            
                            var authString = new String(authDecoded);
            
                            String[] credentials = authString.split(":");
                            String username = credentials[0];
                            String password = credentials[1];
            
            
                            //validate user
                            var user = this.userRepository.findByUsername(username);
            
                            if (user == null) {
                                response.sendError(401,"User does not exists!");
                            } else {
                                //validate pw
                                var passwordVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                                if (passwordVerified.verified) {
                                    request.setAttribute("idUser", user.getId());
                                    filterChain.doFilter(request, response);
                                } else {
                                    
                                response.sendError(401,"Password does not match!");
                                }
                            }
                } else {
                    //segue viagem
                    filterChain.doFilter(request, response);
                }


    }

    
}
