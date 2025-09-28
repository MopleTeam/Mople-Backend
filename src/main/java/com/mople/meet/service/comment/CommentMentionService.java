package com.mople.meet.service.comment;

import com.mople.entity.meet.comment.CommentMention;
import com.mople.entity.user.User;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentMentionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentMentionService {

    private final CommentMentionRepository mentionRepository;
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

    public List<Long> findUserIdByCommentId(Long commentId) {
        return mentionRepository.findUserIdByCommentId(commentId);
    }

    public void deleteByCommentIds(List<Long> replyIds) {
        mentionRepository.deleteByCommentIdIn(replyIds);
    }

    public void deleteByCommentId(Long commentId) {
        mentionRepository.deleteByCommentId(commentId);
    }
}
