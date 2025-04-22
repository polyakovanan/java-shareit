package ru.practicum.shareit.core.item.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.item.persistance.entity.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemIdIn(List<Long> itemIds);

    List<Comment> findAllByItemId(Long itemId);
}
