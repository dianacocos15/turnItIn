package integrations.turnitin.com.membersearcher.service;

import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships,
	 * it then calls to fetch the user details for each user individually and
	 * associates them with their corresponding membership.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList object.
	 */
	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
		CompletableFuture<MembershipList> membershipsFuture = membershipBackendClient.fetchMemberships();

		CompletableFuture<UserList> usersFuture = membershipBackendClient.fetchUsers();

		return membershipsFuture.thenCombine(usersFuture, (memberships, users) -> {
			// Create a map of user ID to User object for efficient lookup
			Map<String, User> userMap = users.getUsers().stream()
					.collect(Collectors.toMap(User::getId, Function.identity()));

			// Associate each membership with its user using the userMap
			memberships.getMemberships().forEach(member -> {
				User user = userMap.get(member.getUserId());
				member.setUser(user);
			});

			return memberships;
		});
	}
}
