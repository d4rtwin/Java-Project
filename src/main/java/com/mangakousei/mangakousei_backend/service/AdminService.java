
package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.repository.TantouMangakaAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final TantouMangakaAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public List<TantouMangakaAssignment> getActiveMangakasForTantou(Long tantouId) {
        return assignmentRepository.findByTantou_UserIdAndIsActiveTrue(tantouId);
    }

    public List<TantouMangakaAssignment> getActiveTantousForMangaka(Long mangakaId) {
        return assignmentRepository.findByMangaka_UserIdAndIsActiveTrue(mangakaId);
    }
}
