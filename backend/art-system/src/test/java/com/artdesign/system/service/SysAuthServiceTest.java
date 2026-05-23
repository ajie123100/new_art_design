package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.domain.dto.UserInfo;
import com.artdesign.system.domain.entity.SysUser;
import com.artdesign.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysAuthServiceTest {
    private SysUserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private SysAuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = mock(SysUserMapper.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new SysAuthService(userMapper, passwordEncoder);
    }

    @Test
    void loginUpgradesPlainPasswordToBcrypt() {
        SysUser user = activeUser();
        user.setPassword("123456");
        when(userMapper.selectOne(anyWrapper())).thenReturn(user);

        SysUser loggedInUser = authService.login("Super", "123456");

        assertThat(loggedInUser).isSameAs(user);
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("123456", user.getPassword())).isTrue();
        assertThat(user.getUpdateBy()).isEqualTo("system");
        assertThat(user.getUpdateTime()).isNotNull();
        verify(userMapper).updateById(user);
    }

    @Test
    void loginKeepsExistingBcryptPassword() {
        SysUser user = activeUser();
        String encryptedPassword = passwordEncoder.encode("123456");
        user.setPassword(encryptedPassword);
        when(userMapper.selectOne(anyWrapper())).thenReturn(user);

        authService.login("Super", "123456");

        assertThat(user.getPassword()).isEqualTo(encryptedPassword);
        verify(userMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void loginRejectsInvalidPassword() {
        SysUser user = activeUser();
        user.setPassword(passwordEncoder.encode("123456"));
        when(userMapper.selectOne(anyWrapper())).thenReturn(user);

        assertThatThrownBy(() -> authService.login("Super", "wrong"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void superRoleReceivesWildcardPermission() {
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("R_SUPER"));

        List<String> permissions = authService.getPermissionList(1L);

        assertThat(permissions).containsExactly("*:*:*");
        verify(userMapper, never()).selectPermissionsByUserId(1L);
    }

    @Test
    void getUserInfoFallsBackToDefaultAvatar() {
        SysUser user = activeUser();
        user.setAvatar("");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("R_ADMIN"));
        when(userMapper.selectButtonsByUserId(1L)).thenReturn(List.of("system:user:list"));

        UserInfo userInfo = authService.getUserInfo(1L);

        assertThat(userInfo.userId()).isEqualTo(1L);
        assertThat(userInfo.userName()).isEqualTo("Super");
        assertThat(userInfo.roles()).containsExactly("R_ADMIN");
        assertThat(userInfo.buttons()).containsExactly("system:user:list");
        assertThat(userInfo.avatar()).isEqualTo("https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png");
    }

    private SysUser activeUser() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("Super");
        user.setEmail("super@example.com");
        user.setStatus("1");
        user.setDelFlag("0");
        return user;
    }

    private Wrapper<SysUser> anyWrapper() {
        return any();
    }
}
