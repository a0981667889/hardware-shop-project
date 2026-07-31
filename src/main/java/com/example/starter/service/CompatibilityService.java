package com.example.starter.service;

import com.example.starter.dto.CompatibilityWarning;
import com.example.starter.entity.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CompatibilityService {

    /**
     * 檢查零件組合的相容性,回傳警告清單(不拋例外)。
     * 清單為空代表完全相容;有內容代表有問題,由呼叫端決定要不要擋下。
     * 目前只實作 CPU vs 主機板 socket 這條規則,之後可擴充更多規則。
     */
    public List<CompatibilityWarning> check(Collection<Component> components) {
        List<CompatibilityWarning> warnings = new ArrayList<>();

        Component cpu = components.stream()
                .filter(c -> "CPU".equalsIgnoreCase(c.getCategory()))
                .findFirst().orElse(null);

        Component motherboard = components.stream()
                .filter(c -> "MOTHERBOARD".equalsIgnoreCase(c.getCategory()))
                .findFirst().orElse(null);
        Component ram = components.stream()
                .filter(c -> "RAM".equalsIgnoreCase(c.getCategory()))
                .findFirst().orElse(null);

        if (cpu != null && motherboard != null) {
            boolean socketMismatch = cpu.getSocket() == null
                    || motherboard.getSocket() == null
                    || !cpu.getSocket().equalsIgnoreCase(motherboard.getSocket());

            if (socketMismatch) {
                warnings.add(new CompatibilityWarning(
                        String.format("CPU「%s」(socket %s) 與主機板「%s」(socket %s) 插槽不相容,可能無法正常安裝",
                                cpu.getName(), cpu.getSocket(), motherboard.getName(), motherboard.getSocket()),
                        List.of(cpu.getId(), motherboard.getId())
                ));
            }
        }
        if (ram != null && motherboard != null) {
            boolean memoryTypeMismatch = ram.getMemoryType() == null
                    || motherboard.getMemoryType() == null
                    || !ram.getMemoryType().equalsIgnoreCase(motherboard.getMemoryType());

            if (memoryTypeMismatch) {
                warnings.add(new CompatibilityWarning(
                        String.format("記憶體「%s」(%s) 與主機板「%s」(支援 %s) 記憶體類型不相容,可能無法安裝",
                                ram.getName(), ram.getMemoryType(), motherboard.getName(), motherboard.getMemoryType()),
                        List.of(ram.getId(), motherboard.getId())
                ));
            }
        }


        return warnings;
    }
}