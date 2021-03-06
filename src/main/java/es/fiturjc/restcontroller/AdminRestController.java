package es.fiturjc.restcontroller;

import java.security.Principal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import es.fiturjc.component.UserComponent;
import es.fiturjc.model.Course;
import es.fiturjc.model.CourseDTO;
import es.fiturjc.model.Schedule;
import es.fiturjc.model.User;
import es.fiturjc.service.AdminService;
import es.fiturjc.service.CourseService;
import es.fiturjc.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

	@Autowired
	private AdminService adminService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserComponent userComponent;

	// ************* USERS *****************

	/**
	 * Get users List
	 *
	 * @return
	 */

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<User>> getUsers() {
		List<User> users = userService.getUsers();
		if (users != null) {
			return new ResponseEntity<>(users, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// Pagination
	@RequestMapping(value = "/paginate/{page}", method = RequestMethod.GET)
	public Page<User> getAllUsers(@PathVariable("page") int page) {
		return userService.findAllusers(new PageRequest(page, 10));
	}

	/**
	 * Get specific user
	 *
	 * @param id
	 * @return
	 */

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<User> getUser(@PathVariable long id) {
		User user = userService.getUserbyID(id);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Delete an User using the id. Checked
	 *
	 * @param id
	 * @return
	 */

	@DeleteMapping(value = "/user/delete/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable long id) {
		User userLogged = userService.findOne(userComponent.getLoggedUser().getId());
		if (userLogged.isAdmin() == true) {
			if (adminService.deleteUser(id)) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * METHOD PATCH TO EDIT THE INFORMATION THAT IS PROVIDED ONLY
	 *
	 * @param id
	 * @param user
	 * @return edited user
	 */

	@PatchMapping(value = "/user/edit/{id}")
	public ResponseEntity<?> editUser(@PathVariable long id, @RequestBody User user) {
		User userLogged = userService.findOne(userComponent.getLoggedUser().getId());

		if (userLogged.isAdmin() == true) {
			User userUpdated = userService.getUserbyID(id);
			if (userUpdated != null) {
				userUpdated = userService.updateUserInfo(id, user);
				return new ResponseEntity<>(userUpdated, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	/****************** COURSES **************/

	/**
	 *
	 *
	 * @return
	 */

	@RequestMapping(value = "/course", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<List<Course>> getCourses(Principal principal) {
		User userLogged = userService.findOne(principal.getName());
		List<Course> courses = courseService.getAllCourses();
		if (userLogged.isAdmin()) {
			if (courses != null) {
				return new ResponseEntity<>(courses, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Get 1 course
	 * 
	 * @param id
	 * @return
	 */

	@RequestMapping(value = "/course/{id}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@JsonView(CourseDetails.class)
	public ResponseEntity<Course> getCourseId(@PathVariable long id) {
		User userLogged = userService.findOne(userComponent.getLoggedUser().getId());
		Course course = courseService.findCourse(id);
		if (userLogged.isAdmin()) {
			if (course != null) {
				return new ResponseEntity<>(course, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	private interface CourseDetails extends Course.Details, Course.Basic, Schedule.Basic {
	}

	/**
	 * @param course
	 * @return
	 */
	@JsonView(CourseDetails.class)
	// @JsonFormat(shape = JsonFormat.Shape.ARRAY)
	@RequestMapping(value = "/course/add", method = RequestMethod.POST)
	public ResponseEntity<Course> addCourse(@RequestBody CourseDTO course, Principal principal) {
		if (principal != null) {
			Course c = courseService.save(course);
			return new ResponseEntity<>(c, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Delete user by id
	 *
	 * @param id
	 * @return
	 */

	@DeleteMapping(value = "/course/delete/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> deleteCourse(@PathVariable long id) {
		User userLogged = userService.findOne(userComponent.getLoggedUser().getId());
		if (userLogged.isAdmin()) {
			if (courseService.deleteCourse(id)) {
				return new ResponseEntity<>(HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	// to do: solve the way to modify schedules

	/**
	 * @param id
	 * @param course
	 * @return
	 */
	@PatchMapping(value = "/course/edit/{id}")
	public ResponseEntity<Course> updateCourse(@PathVariable long id, @RequestBody Course course) {
		User userLogged = userService.findOne(userComponent.getLoggedUser().getId());

		if (userLogged.isAdmin()) {
			Course courseUpdated = courseService.getCourseById(id);
			if (courseUpdated != null) {
				courseUpdated = courseService.updateCourse(id, course);
				return new ResponseEntity<>(courseUpdated, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	// @PostMapping(value= "/course/add1")
	// public ResponseEntity<Course> addCourse (@RequestBody String name,
	// @RequestBody Category category,
	// @RequestParam String description, @RequestParam MultipartFile src,
	// @RequestBody String schedules){
	//
	// User userLogged = userService.findOne(userComponent.getLoggedUser().getId());
	// Course newCourse;
	// if(userLogged.isAdmin()) {
	// String[] schedule = schedules.split(" ");
	// List<Schedule> listSchedule = new ArrayList<Schedule>();
	// for (String item : schedule) {
	// Schedule subSchedule = new Schedule(item);
	// listSchedule.add(subSchedule);
	// }
	// newCourse = courseService.createNewCourse2(name, category, description, src,
	// listSchedule);
	// return new ResponseEntity<>(newCourse, HttpStatus.OK);
	// }
	// return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	// }

}
