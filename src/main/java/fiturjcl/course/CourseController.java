package fiturjcl.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fiturjcl.user.User;
import fiturjcl.user.UserService;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping (value = "/courses")
public class CourseController {
	
	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private UserService userService;
	
//	@Autowired
//	private UserComponent userComponent;
	
	@RequestMapping (value = "")
	public String getCourses(Model model, Principal principal) {
		
		boolean isLogged = principal != null;

		User visitor = (isLogged) ? userService.findByEmail(principal.getName()) : null;
		List<Course> courses = courseRepository.findAll();
		model.addAttribute("courses",courses);
		model.addAttribute("visitor", visitor);
		return "courses";
	}
	
	@RequestMapping (value = "/courses")
	public String userProfile (Model model, HttpServletRequest request) {
		Page<Course> courses = courseRepository.findAll(new PageRequest(0,6));
		model.addAttribute("courses", courses);
		return "courses";
	}
	
	@RequestMapping (value = "/moreCourses")
	public String moreAllShelf(Model model, @RequestParam int page) {
		Page<Course> courses = courseRepository.findAll(new PageRequest(page,6));
		model.addAttribute("course", courses);
		return "list_courses";
}

	
}