package com.example.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.tomcat.util.buf.UriUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class FileController {

  private final Path root = Paths.get("C://Users//user//OneDrive - NITech");

  static class FileItem {
    private String name;
    private boolean isDirectory;

    public FileItem(String name, boolean isDirectory) {
      this.name = name;
      this.isDirectory = isDirectory;
    }

    public String getName() {
      return name;
    }

    public boolean isDirectory() {
      return isDirectory;
    }
  }

  @RequestMapping("/")
  public String getRoot(
      @RequestParam(required = false, defaultValue = "") String path,
      Model model) {
    Path currentPath = root.resolve(path).normalize();

    if (!currentPath.startsWith(root)) {
      currentPath = root;
      path = "";
    }

    try (Stream<Path> stream = Files.list(currentPath)) {
      List<FileItem> list = stream.map(p -> new FileItem(p.getFileName().toString(), Files.isDirectory((p)))).toList();
      model.addAttribute("fileList", list);
    } catch (IOException e) {
      e.printStackTrace();
    }

    model.addAttribute("currentPath", path);
    return "index";
  }

  @PostMapping("/delete")
  public String deleteFile(
      @RequestParam("name") String fileName,
      @RequestParam("parentPath") String parentPath,
      @RequestParam("targetPath") String targetPath,
      RedirectAttributes redirectAttributes) {

    try {
      Path targetFile = root.resolve(targetPath).normalize();
      Files.delete(targetFile);

      redirectAttributes.addFlashAttribute("message", fileName + "を削除しました。");
    } catch (IOException e) {
      e.printStackTrace();
    }
    String encodedpath = UriUtils.encodeQueryParam(parentPath, StandardCharsets.UTF_8);
    return "redirect:/?path=" + encodedpath;
  }

  @PostMapping("/changeFileName")
  public String chnageFileName(
      @RequestParam("oldName") String oldName,
      @RequestParam("newName") String newName,
      @RequestParam("currentPath") String currentPath) {

    try {
      Path parentDir = root.resolve(currentPath).normalize();
      Path oldPath = parentDir.resolve(oldName).normalize();

      String extension = "";
      int index = oldName.lastIndexOf(".");
      if (index >= 0) {
        extension = oldName.substring(index);
      }

      if (!newName.contains(extension)) {
        newName = newName + extension;
      }

      Path newPath = parentDir.resolve(newName).normalize();

      if (!oldPath.startsWith(root) || !newPath.startsWith(root)) {
        throw new SecurityException("不正なパス操作です。");
      }

      Files.move(oldPath, newPath);
    } catch (IOException e) {
      e.printStackTrace();
    }

    String redirectURL = (currentPath == null || currentPath.isEmpty()) ? "redirect:/"
        : "redirect:/?path=" + UriUtils.encodeQueryParam(currentPath, StandardCharsets.UTF_8);
    return redirectURL;

  }

}
