package com.example.molly.follow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.molly.follow.dto.SuggestFollowersDTO;
import com.example.molly.follow.entity.Follow;
import com.example.molly.follow.repository.FollowRepository;
import com.example.molly.user.entity.User;
import com.example.molly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public boolean follow(Long userId, Long targetUserId) {
    User follower = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    User following = userRepository.findById(targetUserId)
        .orElseThrow(() -> new RuntimeException("Target user not found"));
    Optional<Follow> followOptional = followRepository.findByFollowerAndFollowing(follower, following);
    if (followOptional.isPresent()) {
      followRepository.delete(followOptional.get());
      return false;
    } else {
      Follow follow = new Follow();
      follow.setFollower(follower);
      follow.setFollowing(following);
      followRepository.save(follow);
      return true;
    }
  }

  public List<SuggestFollowersDTO> getSuggestFollowers(Long userId, int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    List<User> suggestFollowers = userRepository.findUserNotFollwedByUser(userId, pageable);
    Set<Long> followersIds = followRepository.findByFollowerId(userId).stream()
        .map(follow -> follow.getFollowing().getId()).collect(Collectors.toSet());
    List<SuggestFollowersDTO> suggestFollowerUsers = new ArrayList<>();
    for (User user : suggestFollowers) {
      String message = followersIds.contains(user.getId()) ? "회원님을 팔로우중입니다" : "회원님을 위한 추천";
      suggestFollowerUsers.add(new SuggestFollowersDTO(user, message, false));
    }
    return suggestFollowerUsers;
  }
}
