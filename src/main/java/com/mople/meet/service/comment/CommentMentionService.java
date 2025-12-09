package com.mople.meet.service.comment;

import com.mople.entity.meet.comment.CommentMention;
import com.mople.entity.user.User;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentMentionRepository;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentMentionService {

    private final CommentMentionRepository mentionRepository;
    private final UserRepository userRepository;
    private final EntityReader reader;

    @Transactional
    public void createMentions(List<Long> mentions, Long commentId) {
        if (mentions == null || mentions.isEmpty()) return;

        HashSet<Long> distinctMentions = new HashSet<>(mentions);

        for (Long userId : distinctMentions) {
            User mentionedUser = reader.findUser(userId);

            CommentMention mention = CommentMention.builder()
                    .userId(mentionedUser.getId())
                    .commentId(commentId)
                    .build();
            mentionRepository.save(mention);
        }
    }

    @Transactional
    public void updateMentions(List<Long> mentions, Long commentId) {
        mentionRepository.deleteByCommentId(commentId);
        createMentions(mentions, commentId);
    }


    public List<User> findMentionedUsers(Long commentId) {
        return mentionRepository
                .findCommentMentionByCommentId(commentId)
                .stream()
                .map(CommentMention::getUserId)
                .map(reader::findUser)
                .toList();
    }

    public Map<Long, List<User>> findMentionedUsersInBatch(List<Long> commentIds) {
        List<CommentMention> mentions = mentionRepository.findAllByCommentIdIn(commentIds);

        List<Long> mentionedUserIds = mentions.stream()
                .map(CommentMention::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(mentionedUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return mentions.stream()
                .collect(
                        Collectors.groupingBy(
                                CommentMention::getCommentId,
                                Collectors.mapping(m -> userMap.get(m.getUserId()), Collectors.toList())
                        )
                );
    }

    public List<Long> findUserIdByCommentId(Long commentId) {
        return mentionRepository.findUserIdByCommentId(commentId);
    }
}
