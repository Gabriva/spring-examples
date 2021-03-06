package hello.entity.bidirectional.mappedBy;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import hello.AbstractJpaTest;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@DatabaseSetup("/user_role.xml")
public class RoleTest extends AbstractJpaTest {

    @Test
    public void getUserFromRole() {
        Role role = entityManager.find(Role.class, 1L);

        Set<User> users = role.getUsers();

        assertThat(users.size(), equalTo(2));
        assertThat(users, containsInAnyOrder(
                hasProperty("id", is(1L)),
                hasProperty("id", is(2L))
        ));
    }

    @Test
    public void createRoleWithUser_WhenUserNotSaved_WhenSetToRole_ShouldRelationNotCreated() {
        User user = new User("NewUser");
        Role role = new Role("NewRole");

        role.setUsers(singleton(user));

        Role roleFromDb = entityManager.persistFlushFind(role);
        assertThat(roleFromDb.getUsers().size(), equalTo(0));
    }

    @Test
    public void createRoleWithUser_WhenUserSaved_WhenSetToRole_ShouldRelationNotCreated() {
        User user = new User("NewUser");
        Role role = new Role("NewRole");

        entityManager.persistAndFlush(user);

        role.setUsers(singleton(user));

        Role roleFromDb = entityManager.persistFlushFind(role);
        assertThat(roleFromDb.getUsers().size(), equalTo(0));
    }

    @Test
    public void createRoleWithUser_WhenUserSaved_WhenSetToUser_ShouldRelationCreated() {
        User user = new User("NewUser");
        Role role = new Role("NewRole");

        entityManager.persistAndFlush(user);

        user.setRoles(singleton(role)); // owner

        Role roleFromDb = entityManager.persistFlushFind(role);
        assertThat(roleFromDb.getUsers().size(), equalTo(1));
    }

    @Test
    public void createRoleWithUser_WhenUserSaved_WhenSetToRoleAndUser_ShouldRelationCreated() {
        User user = new User("NewUser");
        Role role = new Role("NewRole");

        entityManager.persistAndFlush(user);

        role.setUsers(singleton(user));
        user.setRoles(singleton(role)); // owner

        Role roleFromDb = entityManager.persistFlushFind(role);
        assertThat(roleFromDb.getUsers().size(), equalTo(1));
    }

    @Test(expected = PersistenceException.class)
    public void deleteRole_ShouldThrowException() {
        Role role = entityManager.find(Role.class, 1L);
        entityManager.remove(role);
        flushAndClean();
    }

    @Test
    public void deleteRole_WhenBeforeDeleteAllRelationsUsers_ShouldBeOk() {
        Role role = entityManager.find(Role.class, 1L);
        Set<User> users = role.getUsers();
        users.forEach(u -> entityManager.remove(u));

        entityManager.remove(role);
        flushAndClean();

        Role roleFromDb = entityManager.find(Role.class, 1L);
        assertThat(roleFromDb, equalTo(null));
    }

    @Test
    public void deleteRole_WhenBeforeDeleteAllRelations_ShouldBeOk() {
        Role role = entityManager.find(Role.class, 1L);
        Set<User> users = role.getUsers();
        // delete only relations not users
        users.forEach(u -> {
            Set<Role> roles = u.getRoles();
            roles.remove(role);
            u.setRoles(roles);
        });

        entityManager.remove(role);
        flushAndClean();

        Role roleFromDb = entityManager.find(Role.class, 1L);
        assertThat(roleFromDb, equalTo(null));
    }
}